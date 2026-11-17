package kz.amixady.link.links.exception;

import java.time.Instant;

public class QuotaExceededException extends RuntimeException {

    private final Instant retryAt;

    public QuotaExceededException(Instant retryAt) {
        super("суточный лимит создания ссылок исчерпан");
        this.retryAt = retryAt;
    }

    public Instant getRetryAt() {
        return retryAt;
    }
}