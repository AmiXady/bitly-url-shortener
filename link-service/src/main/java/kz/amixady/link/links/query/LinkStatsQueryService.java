package kz.amixady.link.links.query;

import kz.amixady.link.links.dto.request.LinkStatsRequest;
import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.exception.InvalidRequestException;
import kz.amixady.link.links.repository.LinkRepository;
import kz.amixady.link.stats.StatsFacade;
import kz.amixady.link.stats.api.LinkStatsResponse;
import kz.amixady.link.stats.api.StatsInterval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * UC-15 Посмотреть статистику — 00-docs/02-use-cases/uc/uc-15.md
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LinkStatsQueryService {

    private static final Duration DEFAULT_PERIOD = Duration.ofDays(30);
    private static final Duration HOURLY_RETENTION = Duration.ofDays(90);

    private final LinkRepository repository;
    private final StatsFacade statsFacade;
    private final Clock clock;

    public LinkStatsResponse getStats(String code, LinkStatsRequest request, UUID ownerId) {
        Instant now = clock.instant();
        Link link = repository.getOwnedByCode(code, ownerId);
        StatsInterval interval = request.intervalOrDefault();

        Instant from = requestedFrom(request, interval, now);
        Instant to = requestedTo(request, interval, now);

        requireHourlyDataAvailable(from, interval, now);

        Instant fromCreation = startOf(link.getCreatedAt(), interval);
        return statsFacade.getStats(code, latest(from, fromCreation), to, interval);
    }

    // начало периода: из запроса или 30 дней назад, вниз до начала часа/дня
    private static Instant requestedFrom(LinkStatsRequest request, StatsInterval interval, Instant now) {
        Instant from = request.hasFrom() ? request.from() : now.minus(DEFAULT_PERIOD);
        return startOf(from, interval);
    }

    // конец периода: из запроса или сейчас, вверх до начала следующего часа/дня — чтобы текущий попал в ряд
    private static Instant requestedTo(LinkStatsRequest request, StatsInterval interval, Instant now) {
        Instant to = request.hasTo() ? request.to() : now;
        return startOfNext(to, interval);
    }

    // UC-15, 3c — по часам данные есть только за последние 90 дней (F-11.3)
    private static void requireHourlyDataAvailable(Instant from, StatsInterval interval, Instant now) {
        Instant oldestHour = startOf(now.minus(HOURLY_RETENTION), interval);
        if (interval.isHourly() && from.isBefore(oldestHour)) {
            throw new InvalidRequestException("по часам — только за последние 90 дней");
        }
    }

    private static Instant startOf(Instant instant, StatsInterval interval) {
        return instant.truncatedTo(interval.isHourly() ? ChronoUnit.HOURS : ChronoUnit.DAYS);
    }

    private static Instant startOfNext(Instant instant, StatsInterval interval) {
        Instant start = startOf(instant, interval);
        return start.equals(instant) ? instant : start.plus(interval.step());
    }

    private static Instant latest(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }
}