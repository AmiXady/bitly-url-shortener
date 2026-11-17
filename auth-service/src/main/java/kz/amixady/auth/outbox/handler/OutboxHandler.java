package kz.amixady.auth.outbox.handler;

import kz.amixady.auth.outbox.persistence.OutboxMessage;
import kz.amixady.auth.outbox.persistence.OutboxType;

import java.util.Set;

public interface OutboxHandler {

    // какие типы сообщений обрабатывает
    Set<OutboxType> types();

    // бросить исключение — значит «не получилось, повторить позже»
    void handle(OutboxMessage message) throws Exception;
}