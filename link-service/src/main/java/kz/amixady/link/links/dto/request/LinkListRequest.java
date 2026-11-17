package kz.amixady.link.links.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kz.amixady.link.links.dto.response.LinkDisplayStatus;

import java.time.Instant;

public record LinkListRequest(
        @Size(min = 3, message = "поиск — от 3 символов")
        String search,
        LinkDisplayStatus status,
        String tag,
        Instant createdFrom,
        Instant createdTo,
        @Min(value = 1, message = "limit — от 1 до 100")
        @Max(value = 100, message = "limit — от 1 до 100")
        Integer limit,
        String cursor
) {
    public static final int DEFAULT_LIMIT = 50;

    @AssertTrue(message = "createdFrom должен быть раньше createdTo")
    public boolean isDateRangeValid() {
        return createdFrom == null || createdTo == null || createdFrom.isBefore(createdTo);
    }

    public int limitOrDefault() {
        return limit == null ? DEFAULT_LIMIT : limit;
    }

    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }

    public boolean hasTag() {
        return tag != null && !tag.isBlank();
    }

    public boolean hasCreatedFrom() {
        return createdFrom != null;
    }

    public boolean hasCreatedTo() {
        return createdTo != null;
    }

    public boolean hasCursor() {
        return cursor != null && !cursor.isBlank();
    }
}