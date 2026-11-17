package kz.amixady.auth.repository;

import kz.amixady.auth.entity.EmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, UUID> {
    Optional<EmailConfirmation> findByTokenHash(String token);
    boolean existsByAccountIdAndCreatedAtAfter(UUID accountId, Instant now);
}
