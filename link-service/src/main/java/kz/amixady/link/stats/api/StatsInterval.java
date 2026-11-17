package kz.amixady.link.stats.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// шаг ряда точек (F-11.3)
public enum StatsInterval {

    HOUR(Duration.ofHours(1), Duration.ofDays(90)),   // по часам — только последние 90 дней
    DAY(Duration.ofDays(1), null);                    // по дням — за всё время

    private final Duration step;
    private final Duration retention;

    StatsInterval(Duration step, Duration retention) {
        this.step = step;
        this.retention = retention;
    }

    public Duration step() {
        return step;
    }

    public boolean isHourly() {
        return this == HOUR;
    }

    public boolean isDaily() {
        return this == DAY;
    }

    public Optional<Instant> earliestAvailable(Instant now) {
        return retention == null ? Optional.empty() : Optional.of(now.minus(retention));
    }
}