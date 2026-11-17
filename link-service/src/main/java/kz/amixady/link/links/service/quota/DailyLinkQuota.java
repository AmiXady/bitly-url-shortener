package kz.amixady.link.links.service.quota;

import java.time.Instant;
import java.util.UUID;

public interface DailyLinkQuota {

    // тратит 1 ссылку из квоты, при превышении бросает QuotaExceededException
    void consume(UUID ownerId, Instant now);

    // возвращает 1 ссылку, если создание не удалось после consume
    void release(UUID ownerId, Instant now);
}