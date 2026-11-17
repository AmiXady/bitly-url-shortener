package kz.amixady.auth.outbox;

import kz.amixady.auth.outbox.handler.OutboxHandler;
import kz.amixady.auth.outbox.persistence.OutboxMessage;
import kz.amixady.auth.outbox.persistence.OutboxMessageRepository;
import kz.amixady.auth.outbox.persistence.OutboxType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Забирает готовые сообщения из outbox и передаёт их handler'ам.
 * Успех — строка удаляется. Ошибка — повтор с экспоненциальной задержкой,
 * после MAX_ATTEMPTS — строка помечается мёртвой (dead_at) и остаётся для разбора.
 */
@Slf4j
@Component
public class OutboxRelay {
    private static final int BATCH_SIZE = 50;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BASE_BACKOFF = Duration.ofSeconds(5);

    private final OutboxMessageRepository repository;
    private final Map<OutboxType, OutboxHandler> handlers = new EnumMap<>(OutboxType.class);
    private final Clock clock;

    public OutboxRelay(OutboxMessageRepository repository, List<OutboxHandler> handlerList, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        registerHandlers(handlerList);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.outbox.relay-delay:2s}")
    public void relay() {
        Instant now = clock.instant();
        for (OutboxMessage message : repository.lockBatch(now, BATCH_SIZE)) {
            process(message, now);
        }
    }

    private void process(OutboxMessage message, Instant now) {
        try {
            handlers.get(message.getType()).handle(message);
            repository.delete(message);
        } catch (Exception e) {
            onFailure(message, e, now);
        }
    }

    // 5s, 10s, 20s, 40s → dead
    private void onFailure(OutboxMessage message, Exception e, Instant now) {
        Duration backoff = BASE_BACKOFF.multipliedBy(1L << message.getAttempts());
        message.fail(e.toString(), backoff, MAX_ATTEMPTS, now);

        if (message.isDead()) {
            log.error("Outbox message {} ({}) is dead after {} attempts",
                    message.getId(), message.getType(), message.getAttempts(), e);
        } else {
            log.warn("Outbox message {} ({}) failed, attempt {}, retry in {}",
                    message.getId(), message.getType(), message.getAttempts(), backoff, e);
        }
    }

    private void registerHandlers(List<OutboxHandler> handlerList) {
        for (OutboxHandler handler : handlerList) {
            for (OutboxType type : handler.types()) {
                if (handlers.putIfAbsent(type, handler) != null) {
                    throw new IllegalStateException("Two handlers for " + type);
                }
            }
        }
        for (OutboxType type : OutboxType.values()) {
            if (!handlers.containsKey(type)) {
                throw new IllegalStateException("No handler for " + type);
            }
        }
    }
}