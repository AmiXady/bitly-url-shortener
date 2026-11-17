package kz.amixady.auth.exception;

public class PasswordResetNotFoundException extends InvalidPasswordResetException {

    public PasswordResetNotFoundException() {
        super("Ссылка недействительна — запросите новую");
    }
}