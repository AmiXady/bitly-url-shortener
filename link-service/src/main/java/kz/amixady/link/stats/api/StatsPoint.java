package kz.amixady.link.stats.api;

import java.time.Instant;

// начало часа или дня (UTC) и число переходов в нём
public record StatsPoint(
        Instant time,
        long clicks
) {
}