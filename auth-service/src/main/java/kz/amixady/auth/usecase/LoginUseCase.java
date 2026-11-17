package kz.amixady.auth.usecase;

import kz.amixady.auth.dto.LoginRequest;
import kz.amixady.auth.dto.TokenResponse;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.Session;
import kz.amixady.auth.exception.InvalidCredentialsException;
import kz.amixady.auth.exception.InvalidRefreshTokenException;
import kz.amixady.auth.exception.TooManyLoginAttemptsException;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.SessionRepository;
import kz.amixady.auth.service.token.AccessToken;
import kz.amixady.auth.service.token.AccessTokenIssuer;
import kz.amixady.auth.service.token.TokenGenerator;
import kz.amixady.auth.service.limit.PasswordAttemptLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * UC-03 Войти — 00-docs/02-use-cases/uc/uc-03.md
 */
@Component
@RequiredArgsConstructor
public class LoginUseCase {

    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final PasswordAttemptLimiter limiter;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    // сценарий А — войти
    public TokenResponse login(LoginRequest request) {
        Instant now = clock.instant();
        EmailAddress email = new EmailAddress(request.email());

        limiter.retryAfter(email).ifPresent(wait -> {
            throw new TooManyLoginAttemptsException(wait);
        });

        Account account = accountRepository.findByEmail(email)
                .filter(found -> found.passwordMatches(request.password(), passwordEncoder))
                .orElseThrow(() -> {
                    limiter.recordFailure(email);
                    return new InvalidCredentialsException();
                });

        limiter.reset(email);
        return startSession(account, now);
    }

    // сценарий Б — продлить вход
    // сценарий Б — продлить вход; отзыв при краже не должен откатиться вместе с ошибкой
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenResponse refresh(String refreshToken) {
        Instant now = clock.instant();
        String hash = tokenGenerator.hash(refreshToken);

        Session session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> {
                    revokeIfReused(hash, now);
                    return new InvalidRefreshTokenException();
                });

        if (!session.isActive(now)) {
            throw new InvalidRefreshTokenException();
        }

        Account account = accountRepository.getAccount(session.getAccountId());

        String newRefreshToken = tokenGenerator.generate();
        session.rotate(tokenGenerator.hash(newRefreshToken), now);

        AccessToken accessToken = accessTokenIssuer.issue(account, session, now);
        return TokenResponse.bearer(accessToken, newRefreshToken);
    }


    private TokenResponse startSession(Account account, Instant now) {
        String refreshToken = tokenGenerator.generate();
        Session session = sessionRepository.save(
                Session.start(account.getId(), tokenGenerator.hash(refreshToken), now));

        AccessToken accessToken = accessTokenIssuer.issue(account, session, now);
        return TokenResponse.bearer(accessToken, refreshToken);
    }


    private void revokeIfReused(String hash, Instant now) {
        sessionRepository.findByPreviousRefreshTokenHash(hash)
                .ifPresent(session -> session.revoke(now));
    }
}