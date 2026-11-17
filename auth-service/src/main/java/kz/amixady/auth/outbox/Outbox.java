package kz.amixady.auth.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.amixady.auth.outbox.persistence.OutboxMessage;
import kz.amixady.auth.outbox.persistence.OutboxMessageRepository;
import kz.amixady.auth.outbox.persistence.OutboxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Outbox {

    private final OutboxMessageRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(propagation = Propagation.MANDATORY)
    public void put(OutboxType type, UUID aggregateId, Object payload) {
        repository.save(OutboxMessage.create(type, aggregateId, toJson(payload), clock.instant()));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox payload", e);
        }
    }
}