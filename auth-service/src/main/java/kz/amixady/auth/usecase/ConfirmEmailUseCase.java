package kz.amixady.auth.usecase;


import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.EmailConfirmation;
import kz.amixady.auth.exception.ConfirmationExpiredException;
import kz.amixady.auth.exception.ConfirmationNotFoundException;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.EmailConfirmationRepository;
import kz.amixady.auth.service.ConfirmationSender;
import kz.amixady.auth.service.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConfirmEmailUseCase {
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(5);

    private final TokenGenerator tokenGenerator;
    private final EmailConfirmationRepository confirmationRepository;
    private final AccountRepository accountRepository;
    private final ConfirmationSender confirmationSender;
    private final Clock clock;


    // сценарий А — подтвердить по токену из письма
    @Transactional
    public void confirm(String token) {

        Instant now = clock.instant();
        EmailConfirmation confirmation = getConfirmation(token);

        if (confirmation.isUsed()) {
            return;
        }
        if (confirmation.isExpired(now)) {
            throw new ConfirmationExpiredException();
        }

        Account account = accountRepository.getAccount(confirmation.getAccountId());

        confirmation.use(now);
        account.confirmEmail();
    }

    @Transactional
    public void resend(String rawEmail) {
        Instant now = clock.instant();
        EmailAddress email = new EmailAddress(rawEmail);

        findUnconfirmedAccount(email)
                .filter(account -> wasSentRecently(account, now))
                .ifPresent(account -> confirmationSender.send(account, now));
    }

    private EmailConfirmation getConfirmation(String token) {
        return confirmationRepository.findByTokenHash(tokenGenerator.hash(token))
                .orElseThrow(ConfirmationNotFoundException::new);
    }

    private Optional<Account> findUnconfirmedAccount(EmailAddress email) {
        return accountRepository.findByEmail(email)
                .filter(account -> !account.isEmailVerified());
    }

    private boolean wasSentRecently(Account account, Instant now) {
        return confirmationRepository.existsByAccountIdAndCreatedAtAfter(
                account.getId(), now.minus(RESEND_INTERVAL));
    }
}
