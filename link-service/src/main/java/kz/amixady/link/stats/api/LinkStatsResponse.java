package kz.amixady.link.stats.api;

import java.time.Instant;
import java.util.List;

// UC-15 — статистика одной ссылки
public record LinkStatsResponse(
        String timezone,
        long total,
        long periodTotal,
        StatsInterval interval,
        Instant from,
        Instant to,
        List<StatsPoint> points
) {

    public static final String UTC = "UTC";

    public LinkStatsResponse {
        points = List.copyOf(points);
    }
}