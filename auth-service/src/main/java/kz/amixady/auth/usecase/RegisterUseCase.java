package kz.amixady.auth.usecase;


import kz.amixady.auth.dto.RegisterRequest;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.event.AlreadyRegisteredNotified;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.service.ConfirmationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RegisterUseCase {

    private final AccountRepository accountRepository;
    private final Clock clock;
    private final PasswordEncoder encoder;
    private final ApplicationEventPublisher events;
    private final ConfirmationSender confirmationSender;


    @Transactional
    public void execute(RegisterRequest request) {
        Instant now = clock.instant();

        EmailAddress email = new EmailAddress(request.email());
        Optional<Account> existing = accountRepository.findByEmail(email);

        if (existing.isPresent()) {
            handleExisting(existing.get(), now);
            return;
        }

        Account account = accountRepository.save(
                Account.register(email, encoder.encode(request.password()), now));
        confirmationSender.send(account, now);
    }

    private void handleExisting(Account account, Instant now) {
        if (account.isEmailVerified()) {
            events.publishEvent(new AlreadyRegisteredNotified(account.getEmail()));
        } else {
            confirmationSender.send(account, now);
        }
    }
}
