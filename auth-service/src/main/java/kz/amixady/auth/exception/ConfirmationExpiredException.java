package kz.amixady.auth.exception;

// UC-02, 3a — ссылка устарела (F-1.4: 24 часа)
public class ConfirmationExpiredException extends InvalidConfirmationException {

    public ConfirmationExpiredException() {
        super("Ссылка устарела — запросите новое письмо");
    }
}