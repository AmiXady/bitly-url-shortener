package kz.amixady.auth.exception;

import java.time.Duration;

// UC-03, 2a — слишком много неудачных попыток (F-12.5); ответ 429
public class TooManyLoginAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyLoginAttemptsException(Duration retryAfter) {
        super("Слишком много попыток входа. Повторите позже");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}