package kz.amixady.auth.repository;

import kz.amixady.auth.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {
    boolean existsByAccountIdAndCreatedAtAfter(UUID accountId, Instant after);
    Optional<PasswordReset> findByTokenHash(String hash);
}
