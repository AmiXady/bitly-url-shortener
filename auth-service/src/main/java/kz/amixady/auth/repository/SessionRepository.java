package kz.amixady.auth.repository;

import jakarta.persistence.LockModeType;
import kz.amixady.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    // UC-03, Б — блокировка: два одновременных продления одним ключом не пройдут оба
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    // BR-4 — пришёл уже использованный ключ
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Session> findByPreviousRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.accountId = :accountId AND s.revokedAt IS NULL")
    int revokeAll(UUID accountId, Instant now);

    // UC-05, F-1.12 — завершить все сессии аккаунта, кроме текущей
    @Modifying
    @Query("""
            UPDATE Session s SET s.revokedAt = :now
            WHERE s.accountId = :accountId
              AND s.id <> :keepSessionId
              AND s.revokedAt IS NULL
            """)
    int revokeAllExcept(@Param("accountId") UUID accountId,
                        @Param("keepSessionId") UUID keepSessionId,
                        @Param("now") Instant now);
}
