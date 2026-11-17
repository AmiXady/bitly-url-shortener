package kz.amixady.link.stats.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// SQL к таблицам link_stats_*; используется только StatsFacade
@Repository
@RequiredArgsConstructor
public class LinkStatsRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // F-11.1 — итоги за всё время по кодам; кодов без переходов в карте нет
    public Map<String, Long> findTotals(Collection<String> codes) {
        String sql = """
                SELECT code, clicks
                FROM link_stats_total
                WHERE code IN (:codes)
                """;

        Map<String, Long> totals = new HashMap<>();
        jdbc.query(sql, new MapSqlParameterSource("codes", codes),
                rs -> {
                    totals.put(rs.getString("code"), rs.getLong("clicks"));
                });
        return totals;
    }

    // F-11.1 — итог одной ссылки; нет строки → 0
    public long findTotal(String code) {
        String sql = """
                SELECT clicks
                FROM link_stats_total
                WHERE code = :code
                """;

        List<Long> result = jdbc.queryForList(sql, new MapSqlParameterSource("code", code), Long.class);
        return result.isEmpty() ? 0 : result.getFirst();
    }

    // F-11.3 — переходы по часам за [from, to); ключ — начало часа (UTC), часов без переходов в карте нет
    public Map<Instant, Long> findHourly(String code, Instant from, Instant to) {
        String sql = """
                SELECT hour, clicks
                FROM link_stats_hourly
                WHERE code = :code AND hour >= :from AND hour < :to
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to));

        Map<Instant, Long> clicks = new HashMap<>();
        jdbc.query(sql, params,
                rs -> {
                    clicks.put(rs.getTimestamp("hour").toInstant(), rs.getLong("clicks"));
                });
        return clicks;
    }

    // F-11.3 — переходы по дням за [from, to); ключ — начало дня (UTC), дней без переходов в карте нет
    public Map<Instant, Long> findDaily(String code, Instant from, Instant to) {
        String sql = """
                SELECT day, clicks
                FROM link_stats_daily
                WHERE code = :code AND day >= :from AND day < :to
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("code", code)
                .addValue("from", toUtcDate(from))
                .addValue("to", toUtcDate(to));

        Map<Instant, Long> clicks = new HashMap<>();
        jdbc.query(sql, params,
                rs -> {
                    clicks.put(startOfDay(rs.getObject("day", LocalDate.class)), rs.getLong("clicks"));
                });
        return clicks;
    }


    public void deleteByCode(String code) {
        MapSqlParameterSource params = new MapSqlParameterSource("code", code);
        jdbc.update("DELETE FROM link_stats_hourly WHERE code = :code", params);
        jdbc.update("DELETE FROM link_stats_daily WHERE code = :code", params);
        jdbc.update("DELETE FROM link_stats_total WHERE code = :code", params);
    }

    private static LocalDate toUtcDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant startOfDay(LocalDate day) {
        return day.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}