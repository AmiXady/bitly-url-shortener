package kz.amixady.link.stats;


import kz.amixady.link.stats.api.ClickTotals;
import kz.amixady.link.stats.api.LinkStatsResponse;
import kz.amixady.link.stats.api.StatsInterval;
import kz.amixady.link.stats.api.StatsPoint;
import kz.amixady.link.stats.repository.LinkStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// вход в модуль статистики
@Component
@RequiredArgsConstructor
public class StatsFacade {

    private final LinkStatsRepository repository;

    // UC-11, F-11.1 — итоги за всё время по кодам порции списка
    public ClickTotals getTotals(Collection<String> codes) {
        if (codes.isEmpty()) {
            return new ClickTotals(Map.of());
        }
        return new ClickTotals(repository.findTotals(codes));
    }

    // UC-15 — статистика одной ссылки за период; from/to уже выровнены и проверены
    public LinkStatsResponse getStats(String code, Instant from, Instant to, StatsInterval interval) {
        Map<Instant, Long> stored = interval.isHourly()
                ? repository.findHourly(code, from, to)
                : repository.findDaily(code, from, to);

        //fill gaps
        List<StatsPoint> points = new ArrayList<>();
        for (Instant t = from; t.isBefore(to); t = t.plus(interval.step())) {
            points.add(new StatsPoint(t, stored.getOrDefault(t, 0L)));
        }

        long total = repository.findTotal(code);
        long periodTotal = points.stream()
                .mapToLong(StatsPoint::clicks)
                .sum();


        return new LinkStatsResponse(LinkStatsResponse.UTC, total, periodTotal, interval, from, to, points);
    }


    public void deleteStats(String code){

    }

}