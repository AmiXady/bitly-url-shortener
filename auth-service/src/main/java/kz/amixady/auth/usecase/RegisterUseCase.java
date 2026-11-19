package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.RegisterRequest;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.EmailConfirmation;
import kz.amixady.auth.notification.Notifications;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.EmailConfirmationRepository;
import kz.amixady.auth.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * UC-01 Зарегистрироваться — 00-docs/02-use-cases/uc/uc-01.md
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterUseCase {

    private final AccountRepository accountRepository;
    private final Clock clock;
    private final PasswordEncoder encoder;
    private final EmailConfirmationRepository confirmationRepository;
    private final TokenGenerator tokenGenerator;
    private final Notifications notifications;

    @Transactional
    public void execute(RegisterRequest request) {
        Instant now = clock.instant();
        EmailAddress email = new EmailAddress(request.email());

        String passwordHash = encoder.encode(request.password());

        Optional<Account> existing = accountRepository.findByEmail(email);
        if (existing.isPresent()) {
            handleExisting(existing.get(), now);
            return;
        }

        Account account = accountRepository.save(Account.register(email, passwordHash, now));
        sendConfirmation(account, now);

        log.info("Account registered: accountId={}", account.getId());
    }

    // 3a, 3b — аккаунт не создаём, ответ тот же
    private void handleExisting(Account account, Instant now) {
        if (account.isEmailVerified()) {
            notifications.alreadyRegistered(account);
        } else {
            sendConfirmation(account, now);
        }
    }

    private void sendConfirmation(Account account, Instant now) {
        String token = tokenGenerator.generate();
        confirmationRepository.save(
                EmailConfirmation.issue(account.getId(), tokenGenerator.hash(token), now));
        notifications.emailConfirmation(account, token);
    }
}