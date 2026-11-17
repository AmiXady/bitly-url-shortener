package kz.amixady.auth.usecase;

import kz.amixady.auth.repository.SessionRepository;
import kz.amixady.auth.service.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * UC-20 Выйти — 00-docs/02-use-cases/uc/uc-20.md
 */
@Component
@RequiredArgsConstructor
public class LogoutUseCase {

    private final SessionRepository sessionRepository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    @Transactional
    public void execute(String refreshToken) {
        Instant now = clock.instant();
        sessionRepository.findByRefreshTokenHash(tokenGenerator.hash(refreshToken))
                .ifPresent(session -> session.revoke(now));
    }

}