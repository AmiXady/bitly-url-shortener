package kz.amixady.auth.limit;

import kz.amixady.auth.entity.EmailAddress;

import java.time.Duration;
import java.util.Optional;

// F-12.5 — ограничение неудачных попыток входа на один email
public interface PasswordAttemptLimiter {

    // сколько ждать, если лимит исчерпан; пусто — входить можно
    Optional<Duration> retryAfter(EmailAddress email);

    // засчитать неудачную попытку
    void recordFailure(EmailAddress email);

    // сбросить счётчик после успешного входа (UC-03, BR-6)
    void reset(EmailAddress email);
}