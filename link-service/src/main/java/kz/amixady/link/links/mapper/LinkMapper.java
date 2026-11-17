package kz.amixady.link.links.mapper;

import kz.amixady.link.links.dto.response.LinkDisplayStatus;
import kz.amixady.link.links.dto.LinkListItem;
import kz.amixady.link.links.dto.response.LinkResponse;
import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.query.LinkRow;
import kz.amixady.link.stats.api.ClickTotals;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class LinkMapper {

    private final String baseUrl;

    public LinkMapper(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public LinkResponse toResponse(Link link, Instant now) {
        return new LinkResponse(
                baseUrl + "/" + link.getCode(),
                link.getCode(),
                link.getUrl(),
                link.getPendingUrl(),
                link.getPendingUrlRejection(),
                link.getTitle(),
                List.copyOf(link.getTags()),
                link.getExpiresAt(),
                LinkDisplayStatus.of(link.getStatus(), link.getExpiresAt(), now),
                link.getSafetyStatus(),
                link.getCreatedAt()
        );
    }


    public List<LinkListItem> toListItems(List<LinkRow> rows, ClickTotals totals, Instant now) {
        return rows.stream()
                .map(row -> toListItem(row, totals.of(row.code()), now))
                .toList();
    }

    public LinkListItem toListItem(LinkRow row, long clicks, Instant now) {
        return new LinkListItem(
                baseUrl + "/" + row.code(),
                row.code(),
                row.title(),
                row.url(),
                row.tags(),
                row.createdAt(),
                LinkDisplayStatus.of(row.status(), row.expiresAt(), now),
                row.safetyStatus(),
                clicks
        );
    }
}