package kz.amixady.auth.exception;

public class WrongCurrentPasswordException extends RuntimeException {
    public WrongCurrentPasswordException() {
        super("Текущий пароль неверный");
    }
}