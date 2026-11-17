package kz.amixady.auth.service;

import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailConfirmation;
import kz.amixady.auth.event.EmailConfirmationRequested;
import kz.amixady.auth.repository.EmailConfirmationRepository;
import kz.amixady.auth.service.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

// выдать ссылку подтверждения и запросить письмо (UC-01, UC-02)
@Component
@RequiredArgsConstructor
public class ConfirmationSender {

    private final EmailConfirmationRepository confirmationRepository;
    private final TokenGenerator tokenGenerator;
    private final ApplicationEventPublisher events;

    public void send(Account account, Instant now) {
        String token = tokenGenerator.generate();
        confirmationRepository.save(
                EmailConfirmation.issue(account.getId(), tokenGenerator.hash(token), now));
        events.publishEvent(new EmailConfirmationRequested(account.getEmail(), token));
    }
}