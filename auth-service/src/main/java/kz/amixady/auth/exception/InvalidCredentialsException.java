package kz.amixady.auth.exception;

// UC-03, 3a — нет аккаунта или неверный пароль; ответ одинаковый (F-1.7)
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Неверный email или пароль");
    }
}