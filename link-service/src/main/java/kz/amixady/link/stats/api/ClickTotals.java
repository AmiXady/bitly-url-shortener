package kz.amixady.link.stats.api;

import java.util.Map;

public record ClickTotals(
        Map<String, Long> byCode
) {
    public ClickTotals {
        byCode = Map.copyOf(byCode);
    }

    public long of(String code) {
        return byCode.getOrDefault(code, 0L);
    }
}