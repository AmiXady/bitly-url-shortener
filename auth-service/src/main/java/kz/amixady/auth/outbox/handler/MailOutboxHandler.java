package kz.amixady.auth.outbox.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.notification.mail.MailService;
import kz.amixady.auth.outbox.persistence.OutboxMessage;
import kz.amixady.auth.outbox.persistence.OutboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static kz.amixady.auth.outbox.persistence.OutboxType.*;

@Component
@RequiredArgsConstructor
class MailOutboxHandler implements OutboxHandler {

    private final MailService mailService;
    private final ObjectMapper objectMapper;

    @Override
    public Set<OutboxType> types() {
        return Set.of(MAIL_EMAIL_CONFIRMATION, MAIL_PASSWORD_RESET,
                MAIL_PASSWORD_CHANGED, MAIL_ALREADY_REGISTERED);
    }

    @Override
    public void handle(OutboxMessage message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        EmailAddress to = new EmailAddress(payload.get("to").asText());

        switch (message.getType()) {
            case MAIL_EMAIL_CONFIRMATION -> mailService.sendEmailConfirmation(to, payload.get("token").asText());
            case MAIL_PASSWORD_RESET     -> mailService.sendPasswordReset(to, payload.get("token").asText());
            case MAIL_PASSWORD_CHANGED   -> mailService.sendPasswordChanged(to);
            case MAIL_ALREADY_REGISTERED -> mailService.sendAlreadyRegistered(to);
            default -> throw new IllegalArgumentException("Unsupported type: " + message.getType());
        }
    }
}