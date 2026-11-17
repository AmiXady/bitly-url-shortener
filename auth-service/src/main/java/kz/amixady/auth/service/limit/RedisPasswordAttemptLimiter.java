package kz.amixady.auth.service.limit;

import kz.amixady.auth.entity.EmailAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisPasswordAttemptLimiter implements PasswordAttemptLimiter {
    private static final int MAX_FAILURES = 10;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final String KEY_PREFIX = "auth:password:fail:";

    private final StringRedisTemplate redis;


    @Override
    public Optional<Duration> retryAfter(EmailAddress email) {
        String key = key(email);

        String value = redis.opsForValue().get(key);
        if (value == null || Long.parseLong(value) < MAX_FAILURES) {
            return Optional.empty();
        }

        Long secondsLeft = redis.getExpire(key);
        long wait = secondsLeft == null || secondsLeft < 1 ? 1 : secondsLeft;
        return Optional.of(Duration.ofSeconds(wait));
    }

    @Override
    public void recordFailure(EmailAddress email) {
        String key = key(email);
        Long failures = redis.opsForValue().increment(key);
        if (failures != null && failures == 1) {
            redis.expire(key, WINDOW);
        }
    }

    @Override
    public void reset(EmailAddress email) {
        redis.delete(key(email));
    }


    private static String key(EmailAddress email) {
        return KEY_PREFIX + email.value();
    }
}
