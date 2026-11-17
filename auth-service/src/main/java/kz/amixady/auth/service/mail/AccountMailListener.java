package kz.amixady.auth.service.mail;

import kz.amixady.auth.event.AlreadyRegisteredNotified;
import kz.amixady.auth.event.EmailConfirmationRequested;
import kz.amixady.auth.event.PasswordChanged;
import kz.amixady.auth.event.PasswordResetRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AccountMailListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(EmailConfirmationRequested event) {
        mailService.sendEmailConfirmation(event.email(), event.token());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AlreadyRegisteredNotified event) {
        mailService.sendAlreadyRegistered(event.email());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PasswordResetRequested event) {
        mailService.sendPasswordReset(event.email(), event.token());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(PasswordChanged event) {
        mailService.sendPasswordChanged(event.email());
    }
}