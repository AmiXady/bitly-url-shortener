package kz.amixady.auth.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidConfirmationException.class)
    public ProblemDetail invalidConfirmation(InvalidConfirmationException e) {
        return problem(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(InvalidPasswordResetException.class)
    public ProblemDetail invalidPasswordReset(InvalidPasswordResetException e) {
        return problem(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(WrongCurrentPasswordException.class)
    public ProblemDetail wrongCurrentPassword(WrongCurrentPasswordException e) {
        return problem(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail invalidCredentials(InvalidCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, e);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail invalidRefreshToken(InvalidRefreshTokenException e) {
        return problem(HttpStatus.UNAUTHORIZED, e);
    }

    @ExceptionHandler(TooManyPasswordAttemptsException.class)
    public ResponseEntity<ProblemDetail> tooManyPasswordAttempts(TooManyPasswordAttemptsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfter().toSeconds()))
                .body(problem(HttpStatus.TOO_MANY_REQUESTS, e));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail accountNotFound(AccountNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка");
    }

    private static ProblemDetail problem(HttpStatus status, RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(status, e.getMessage());
    }
}