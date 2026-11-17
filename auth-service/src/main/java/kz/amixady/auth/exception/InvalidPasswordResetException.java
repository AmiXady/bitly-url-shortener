package kz.amixady.auth.exception;

// UC-04, Б — ссылка сброса не подходит; ответ 400
public abstract class InvalidPasswordResetException extends RuntimeException {

    protected InvalidPasswordResetException(String message) {
        super(message);
    }
}