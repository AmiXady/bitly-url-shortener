package kz.amixady.link.links.dto.request;

import jakarta.validation.constraints.AssertTrue;
import kz.amixady.link.stats.api.StatsInterval;

import java.time.Instant;

// UC-15 — параметры статистики; всё необязательно
public record LinkStatsRequest(
        Instant from,
        Instant to,
        StatsInterval interval
) {

    public static final StatsInterval DEFAULT_INTERVAL = StatsInterval.DAY;

    // 3b
    @AssertTrue(message = "from должен быть раньше to")
    public boolean isPeriodValid() {
        return from == null || to == null || from.isBefore(to);
    }

    public StatsInterval intervalOrDefault() {
        return interval == null ? DEFAULT_INTERVAL : interval;
    }

    public boolean hasFrom() {
        return from != null;
    }

    public boolean hasTo() {
        return to != null;
    }
}