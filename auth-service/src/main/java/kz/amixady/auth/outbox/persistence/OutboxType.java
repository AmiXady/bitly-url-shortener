package kz.amixady.auth.outbox.persistence;

public enum OutboxType {
    MAIL_EMAIL_CONFIRMATION,
    MAIL_PASSWORD_RESET,
    MAIL_PASSWORD_CHANGED,
    MAIL_ALREADY_REGISTERED
}