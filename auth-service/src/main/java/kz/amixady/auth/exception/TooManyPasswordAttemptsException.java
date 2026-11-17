package kz.amixady.auth.exception;

import java.time.Duration;

// F-12.5 — слишком много неверных попыток ввода пароля (вход UC-03, смена UC-05); ответ 429
public class TooManyPasswordAttemptsException extends RuntimeException {

    private final Duration retryAfter;

    public TooManyPasswordAttemptsException(Duration retryAfter) {
        super("Слишком много неверных попыток. Повторите позже");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}