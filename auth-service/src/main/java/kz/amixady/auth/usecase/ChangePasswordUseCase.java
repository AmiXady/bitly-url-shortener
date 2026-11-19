package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.ChangePasswordRequest;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.exception.TooManyPasswordAttemptsException;
import kz.amixady.auth.exception.WrongCurrentPasswordException;
import kz.amixady.auth.limit.PasswordAttemptLimiter;
import kz.amixady.auth.notification.Notifications;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * UC-05 Сменить пароль — 00-docs/02-use-cases/uc/uc-05.md
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final PasswordAttemptLimiter attemptLimiter;
    private final PasswordEncoder passwordEncoder;
    private final Notifications notifications;
    private final Clock clock;

    @Transactional
    public void execute(ChangePasswordRequest request, UUID accountId, UUID currentSessionId) {
        Instant now = clock.instant();
        Account account = accountRepository.getAccount(accountId);

        verifyCurrentPassword(account, request.currentPassword());

        account.changePassword(request.newPassword(), passwordEncoder);
        sessionRepository.revokeAllExcept(accountId, currentSessionId, now);
        notifications.passwordChanged(account);

        log.info("Password changed: accountId={}, keptSessionId={}", accountId, currentSessionId);
    }

    // 3a, 4a, BR-1, BR-3 — текущий пароль обязателен, неверные попытки ограничены
    private void verifyCurrentPassword(Account account, String currentPassword) {
        EmailAddress email = account.getEmail();

        attemptLimiter.retryAfter(email).ifPresent(wait -> {
            // с действующим токеном подбирают пароль — возможно, токен украден
            log.warn("Password change blocked by attempt limit: accountId={}, retryAfter={}",
                    account.getId(), wait);
            throw new TooManyPasswordAttemptsException(wait);
        });

        if (!account.passwordMatches(currentPassword, passwordEncoder)) {
            attemptLimiter.recordFailure(email);
            throw new WrongCurrentPasswordException();
        }

        attemptLimiter.reset(email);
    }
}