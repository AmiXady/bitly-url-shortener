package kz.amixady.link.links.repository;

import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.entity.LinkStatus;
import kz.amixady.link.links.exception.LinkNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {
    Optional<Link> findByCode(String code);

    @Query(value = """
            SELECT * FROM links
            WHERE owner_id = :ownerId
              AND url = :url
              AND custom_code = false
              AND status = 'ACTIVE'
              AND (expires_at IS NULL OR expires_at > :now)
            ORDER BY created_at
            LIMIT 1
            """, nativeQuery = true)
    Optional<Link> findReusable(UUID ownerId, String url, Instant now);

    boolean existsByCode(String code);

    default Link getOwnedByCode(String code, UUID ownerId) {
        return findByCodeAndOwnerIdAndStatusNot(code, ownerId, LinkStatus.DELETED)
                .orElseThrow(() -> new LinkNotFoundException(code));
    }

    Optional<Link> findByCodeAndOwnerIdAndStatusNot(String code, UUID ownerId, LinkStatus status);
    Optional<Link> findByCodeAndOwnerId(String code, UUID ownerId);


}
