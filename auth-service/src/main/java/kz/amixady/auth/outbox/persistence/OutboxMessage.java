package kz.amixady.auth.outbox.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    private static final int MAX_ERROR_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 64)
    private OutboxType type;

    @Column(name = "aggregate_id", updatable = false)
    private UUID aggregateId;

    // JSON; может содержать сырой токен — строка удаляется сразу после обработки
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, updatable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private int attempts;

    // relay берёт только строки, у которых next_attempt_at <= now
    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = MAX_ERROR_LENGTH)
    private String lastError;

    // попытки исчерпаны — relay больше не берёт, разбирать вручную
    @Column(name = "dead_at")
    private Instant deadAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static OutboxMessage create(OutboxType type, UUID aggregateId, String payload, Instant now) {
        OutboxMessage message = new OutboxMessage();
        message.type = type;
        message.aggregateId = aggregateId;
        message.payload = payload;
        message.attempts = 0;
        message.nextAttemptAt = now;
        message.createdAt = now;
        return message;
    }

    // неудача: повторить позже или, если попытки кончились, пометить мёртвым
    public void fail(String error, Duration backoff, int maxAttempts, Instant now) {
        this.attempts++;
        this.lastError = truncate(error);
        if (attempts >= maxAttempts) {
            this.deadAt = now;
        } else {
            this.nextAttemptAt = now.plus(backoff);
        }
    }

    public boolean isDead() {
        return deadAt != null;
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}