package kz.amixady.auth.notification;

import kz.amixady.auth.entity.Account;
import kz.amixady.auth.outbox.Outbox;
import kz.amixady.auth.outbox.persistence.OutboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Уведомления пользователю.
 * Письма не отправляются сразу: сообщение кладётся в outbox в транзакции use case'а,
 * OutboxRelay по расписанию забирает его и отправляет через MailService.
 * Доставка at-least-once — письмо может прийти дважды.
 */
@Component
@RequiredArgsConstructor
public class Notifications {

    private final Outbox outbox;

    public void emailConfirmation(Account account, String token) {
        outbox.put(OutboxType.MAIL_EMAIL_CONFIRMATION, account.getId(),
                Map.of("to", account.getEmail().value(), "token", token));
    }

    public void passwordReset(Account account, String token) {
        outbox.put(OutboxType.MAIL_PASSWORD_RESET, account.getId(),
                Map.of("to", account.getEmail().value(), "token", token));
    }

    public void passwordChanged(Account account) {
        outbox.put(OutboxType.MAIL_PASSWORD_CHANGED, account.getId(),
                Map.of("to", account.getEmail().value()));
    }

    public void alreadyRegistered(Account account) {
        outbox.put(OutboxType.MAIL_ALREADY_REGISTERED, account.getId(),
                Map.of("to", account.getEmail().value()));
    }
}