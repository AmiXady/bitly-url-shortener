package kz.amixady.auth.notification.mail;

import kz.amixady.auth.entity.EmailAddress;

public interface MailService {
    void sendEmailConfirmation(EmailAddress to, String token);

    void sendAlreadyRegistered(EmailAddress to);

    void sendPasswordReset(EmailAddress to, String token);

    void sendPasswordChanged(EmailAddress to);
}