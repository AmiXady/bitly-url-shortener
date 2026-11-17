package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.ForgotPasswordRequest;
import kz.amixady.auth.dto.ResetPasswordRequest;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.PasswordReset;
import kz.amixady.auth.event.PasswordChanged;
import kz.amixady.auth.event.PasswordResetRequested;
import kz.amixady.auth.exception.PasswordResetExpiredException;
import kz.amixady.auth.exception.PasswordResetNotFoundException;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.PasswordResetRepository;
import kz.amixady.auth.repository.SessionRepository;
import kz.amixady.auth.service.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * UC-04 Восстановить пароль — 00-docs/02-use-cases/uc/uc-04.md
 */
@Component
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(5);

    private final AccountRepository accountRepository;
    private final PasswordResetRepository resetRepository;
    private final SessionRepository sessionRepository;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    // сценарий А — запросить ссылку; ответ всегда одинаковый (BR-2)
    @Transactional
    public void requestLink(ForgotPasswordRequest request) {
        Instant now = clock.instant();

        accountRepository.findByEmail(new EmailAddress(request.email()))
                .filter(account -> !wasSentRecently(account, now))
                .ifPresent(account -> sendResetLink(account, now));
    }

    // сценарий Б — задать новый пароль по ссылке из письма
    @Transactional
    public void reset(ResetPasswordRequest request) {
        Instant now = clock.instant();

        PasswordReset reset = getUsableReset(request.token(), now);
        Account account = accountRepository.getAccount(reset.getAccountId());

        reset.use(now);
        account.changePassword(request.newPassword(), passwordEncoder);
        account.confirmEmail();
        sessionRepository.revokeAll(account.getId(), now);
        events.publishEvent(new PasswordChanged(account.getEmail()));
    }

    // 3a — ссылки нет; 4a — использована или истекла
    private PasswordReset getUsableReset(String token, Instant now) {
        PasswordReset reset = resetRepository.findByTokenHash(tokenGenerator.hash(token))
                .orElseThrow(PasswordResetNotFoundException::new);

        if (!reset.isUsable(now)) {
            throw new PasswordResetExpiredException();
        }
        return reset;
    }

    // BR-4 — не чаще одного письма сброса за RESEND_INTERVAL
    private boolean wasSentRecently(Account account, Instant now) {
        return resetRepository.existsByAccountIdAndCreatedAtAfter(
                account.getId(), now.minus(RESEND_INTERVAL));
    }

    private void sendResetLink(Account account, Instant now) {
        String token = tokenGenerator.generate();
        resetRepository.save(PasswordReset.issue(account.getId(), tokenGenerator.hash(token), now));
        events.publishEvent(new PasswordResetRequested(account.getEmail(), token));
    }
}