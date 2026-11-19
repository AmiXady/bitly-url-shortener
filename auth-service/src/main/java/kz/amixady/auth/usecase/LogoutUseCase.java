package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.RefreshTokenRequest;
import kz.amixady.auth.entity.Session;
import kz.amixady.auth.repository.SessionRepository;
import kz.amixady.auth.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * UC-20 Выйти — 00-docs/02-use-cases/uc/uc-20.md
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutUseCase {

    private final SessionRepository sessionRepository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    @Transactional
    public void execute(RefreshTokenRequest request) {
        Instant now = clock.instant();
        sessionRepository.findByRefreshTokenHash(tokenGenerator.hash(request.refreshToken()))
                .filter(session -> session.isActive(now))
                .ifPresent(session -> endSession(session, now));
    }

    private void endSession(Session session, Instant now) {
        session.revoke(now);
        log.info("Logout: accountId={}, sessionId={}", session.getAccountId(), session.getId());
    }

}