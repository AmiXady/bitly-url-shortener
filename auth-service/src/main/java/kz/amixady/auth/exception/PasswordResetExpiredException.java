package kz.amixady.auth.exception;

public class PasswordResetExpiredException extends InvalidPasswordResetException {

    public PasswordResetExpiredException() {
        super("Ссылка устарела — запросите новую");
    }
}