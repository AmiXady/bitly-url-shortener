package kz.amixady.auth.usecase;

import jakarta.annotation.PostConstruct;
import kz.amixady.auth.dto.LoginRequest;
import kz.amixady.auth.dto.RefreshTokenRequest;
import kz.amixady.auth.dto.TokenResponse;
import kz.amixady.auth.entity.Account;
import kz.amixady.auth.entity.EmailAddress;
import kz.amixady.auth.entity.Session;
import kz.amixady.auth.exception.InvalidCredentialsException;
import kz.amixady.auth.exception.InvalidRefreshTokenException;
import kz.amixady.auth.exception.TooManyPasswordAttemptsException;
import kz.amixady.auth.limit.PasswordAttemptLimiter;
import kz.amixady.auth.repository.AccountRepository;
import kz.amixady.auth.repository.SessionRepository;
import kz.amixady.auth.token.AccessToken;
import kz.amixady.auth.token.AccessTokenIssuer;
import kz.amixady.auth.token.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-03 Войти — 00-docs/02-use-cases/uc/uc-03.md
 */
@Slf4j
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

    // если аккаунта нет — проверяем пароль против этого хэша, чтобы ответ занял столько же времени
    private String dummyPasswordHash;

    @PostConstruct
    void initDummyHash() {
        dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    // сценарий А — войти
    public TokenResponse login(LoginRequest request) {
        Instant now = clock.instant();
        EmailAddress email = new EmailAddress(request.email());

        limiter.retryAfter(email).ifPresent(wait -> {
            throw new TooManyPasswordAttemptsException(wait);
        });

        Optional<Account> found = accountRepository.findByEmail(email);

        // BR-5 — «нет аккаунта» и «неверный пароль» неотличимы даже по времени ответа
        if (found.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
        }

        Account account = found
                .filter(a -> a.passwordMatches(request.password(), passwordEncoder))
                .orElseThrow(() -> {
                    limiter.recordFailure(email);
                    return new InvalidCredentialsException();
                });

        limiter.reset(email);
        return startSession(account, now);
    }

    // сценарий Б — продлить вход; отзыв при краже не должен откатиться вместе с ошибкой
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public TokenResponse refresh(RefreshTokenRequest request) {
        Instant now = clock.instant();
        String hash = tokenGenerator.hash(request.refreshToken());

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

        log.info("Login: accountId={}, sessionId={}", account.getId(), session.getId());

        AccessToken accessToken = accessTokenIssuer.issue(account, session, now);
        return TokenResponse.bearer(accessToken, refreshToken);
    }

    // 2b, BR-4 — старый ключ продления предъявили повторно: его украли, завершаем сессию
    private void revokeIfReused(String hash, Instant now) {
        sessionRepository.findByPreviousRefreshTokenHash(hash).ifPresent(session -> {
            session.revoke(now);
            log.warn("Refresh token reuse detected, session revoked: accountId={}, sessionId={}",
                    session.getAccountId(), session.getId());
        });
    }
}