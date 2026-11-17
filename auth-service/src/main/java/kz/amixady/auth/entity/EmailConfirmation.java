package kz.amixady.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_confirmations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
    public class EmailConfirmation {

    public static final Duration TTL = Duration.ofHours(24);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static EmailConfirmation issue(UUID accountId, String tokenHash, Instant now) {
        EmailConfirmation token = new EmailConfirmation();
        token.accountId = accountId;
        token.tokenHash = tokenHash;
        token.createdAt = now;
        token.expiresAt = now.plus(TTL);
        return token;
    }

    public void use(Instant now) {
        this.usedAt = now;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}