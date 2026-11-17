package kz.amixady.link.links.entity;

import jakarta.persistence.*;
import kz.amixady.link.links.exception.LinkStateConflictException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30, updatable = false)
    private String code;

    @Column(name = "custom_code", nullable = false, updatable = false)
    private boolean customCode;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "pending_url", length = 2048)
    private String pendingUrl;

    @Column(name = "pending_url_rejection", length = 500)
    private String pendingUrlRejection;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(length = 200)
    private String title;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> tags = new ArrayList<>();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LinkStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "safety_status", nullable = false, length = 20)
    private SafetyStatus safetyStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // UC-08
    public static Link create(String code, boolean customCode, String url, UUID ownerId,
                              String title, List<String> tags, Instant expiresAt, Instant now) {
        Link link = new Link();
        link.code = code;
        link.customCode = customCode;
        link.url = url;
        link.ownerId = ownerId;
        link.title = title;
        link.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        link.expiresAt = expiresAt;
        link.status = LinkStatus.ACTIVE;
        link.safetyStatus = SafetyStatus.NOT_CHECKED;
        link.createdAt = now;
        return link;
    }

    // ---------- состояние ----------

    public boolean isActive(Instant now) {
        return status == LinkStatus.ACTIVE && !isExpired(now);
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean isArchived() {
        return status == LinkStatus.ARCHIVED;
    }

    public boolean canBeReusedFor(UUID ownerId, String url, Instant now) {
        return isActive(now)
                && this.ownerId.equals(ownerId)
                && Objects.equals(this.url, url);
    }


    //COMMANDS
    public void update(String title, List<String> tags, String url, Instant now) {
        requireActive(now);
        if (title != null) {
            this.title = title;
        }
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
        if (url != null) {
            requestUrlChange(url);
        }
    }

    private void requestUrlChange(String newUrl) {
        if (newUrl.equals(this.url)) {
            this.pendingUrl = null;
            return;
        }
        this.pendingUrl = newUrl;
        this.pendingUrlRejection = null;
    }


    public void changeExpiration(Instant expiresAt) {
        requireNotArchived();
        this.expiresAt = expiresAt;
    }

    public void removeExpiration() {
        requireNotArchived();
        this.expiresAt = null;
    }


    public void archive() {
        requireNotDeleted();
        this.status = LinkStatus.ARCHIVED;
    }

    public void unarchive() {
        requireNotDeleted();
        if (isArchived()) {
            this.status = LinkStatus.ACTIVE;
        }
    }

    public void delete() {
        if (status == LinkStatus.DELETED) {
            return;
        }
        this.status = LinkStatus.DELETED;
        this.url = null;
        this.pendingUrl = null;
        this.pendingUrlRejection = null;
        this.title = null;
        this.tags = new ArrayList<>();
    }

    // ---------- проверки ----------

    private void requireActive(Instant now) {
        requireNotArchived();
        if (isExpired(now)) {
            throw new LinkStateConflictException("истёкшую ссылку можно только продлить");
        }
    }

    private void requireNotArchived() {
        if (isArchived()) {
            throw new LinkStateConflictException("сначала верните ссылку из архива");
        }
    }

    private void requireNotDeleted() {
        if (status == LinkStatus.DELETED) {
            throw new IllegalStateException("Link " + code + " is deleted");
        }
    }
}