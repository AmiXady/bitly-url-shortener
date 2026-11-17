package kz.amixady.auth.exception;

// UC-02, А — ссылка подтверждения не подходит; ответ 400
public abstract class InvalidConfirmationException extends RuntimeException {

    protected InvalidConfirmationException(String message) {
        super(message);
    }
}