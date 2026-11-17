package kz.amixady.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Сессия недействительна — войдите снова");
    }
}