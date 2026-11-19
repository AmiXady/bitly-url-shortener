package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.ConfirmRequest;
import kz.amixady.auth.dto.ResendRequest;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.EmailConfirmation;
import kz.amixady.auth.exception.ConfirmationExpiredException;
import kz.amixady.auth.exception.ConfirmationNotFoundException;
import kz.amixady.auth.notification.Notifications;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.EmailConfirmationRepository;
import kz.amixady.auth.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * UC-02 Подтвердить email — 00-docs/02-use-cases/uc/uc-02.md
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmEmailUseCase {

    // BR-4 — не чаще одного письма за это время
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(5);

    private final TokenGenerator tokenGenerator;
    private final EmailConfirmationRepository confirmationRepository;
    private final AccountRepository accountRepository;
    private final Notifications notifications;
    private final Clock clock;

    // сценарий А — подтвердить по токену из письма
    @Transactional
    public void confirm(ConfirmRequest request) {
        Instant now = clock.instant();
        EmailConfirmation confirmation = getConfirmation(request.token());

        // 3b — уже подтверждён этой ссылкой, повторное нажатие не ошибка
        if (confirmation.isUsed()) {
            return;
        }
        // 3a
        if (confirmation.isExpired(now)) {
            throw new ConfirmationExpiredException();
        }

        Account account = accountRepository.getAccount(confirmation.getAccountId());
        confirmation.use(now);
        account.confirmEmail();

        log.info("Email confirmed: accountId={}", account.getId());
    }

    // сценарий Б — отправить письмо ещё раз; ответ всегда одинаковый (BR-5)
    @Transactional
    public void resend(ResendRequest request) {
        Instant now = clock.instant();
        EmailAddress email = new EmailAddress(request.email());

        findUnconfirmedAccount(email)
                .filter(account -> !wasSentRecently(account, now))
                .ifPresent(account -> sendConfirmation(account, now));
    }

    private EmailConfirmation getConfirmation(String token) {
        return confirmationRepository.findByTokenHash(tokenGenerator.hash(token))
                .orElseThrow(ConfirmationNotFoundException::new);
    }

    // 2a — нет аккаунта или уже подтверждён
    private Optional<Account> findUnconfirmedAccount(EmailAddress email) {
        return accountRepository.findByEmail(email)
                .filter(account -> !account.isEmailVerified());
    }

    // 3a, BR-4
    private boolean wasSentRecently(Account account, Instant now) {
        boolean recently = confirmationRepository.existsByAccountIdAndCreatedAtAfter(
                account.getId(), now.minus(RESEND_INTERVAL));
        if (recently) {
            log.debug("Confirmation resend skipped, sent recently: accountId={}", account.getId());
        }
        return recently;
    }

    private void sendConfirmation(Account account, Instant now) {
        String token = tokenGenerator.generate();
        confirmationRepository.save(
                EmailConfirmation.issue(account.getId(), tokenGenerator.hash(token), now));
        notifications.emailConfirmation(account, token);

        log.info("Confirmation resent: accountId={}", account.getId());
    }
}