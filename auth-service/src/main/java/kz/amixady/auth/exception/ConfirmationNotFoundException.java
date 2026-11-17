package kz.amixady.auth.exception;

// UC-02, 2a — токена нет в системе
public class ConfirmationNotFoundException extends InvalidConfirmationException {

    public ConfirmationNotFoundException() {
        super("Ссылка недействительна — запросите новое письмо");
    }
}