package kz.amixady.link.links.query;

import kz.amixady.link.links.dto.LinkListItem;
import kz.amixady.link.links.dto.request.LinkListRequest;
import kz.amixady.link.links.dto.LinkPage;
import kz.amixady.link.links.dto.response.LinkResponse;
import kz.amixady.link.links.mapper.LinkMapper;
import kz.amixady.link.links.repository.LinkRepository;
import kz.amixady.link.stats.api.ClickTotals;
import kz.amixady.link.stats.StatsFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * UC-11 Найти свою ссылку — 00-docs/02-use-cases/uc/uc-11.md
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LinkQueryService {

    private final LinkRepository repository;
    private final LinkSearchQuery searchQuery;
    private final StatsFacade statsFacade;
    private final LinkMapper mapper;
    private final Clock clock;

    // сценарий Б — GET /links/{code}
    public LinkResponse getByCode(String code, UUID ownerId) {
        return mapper.toResponse(repository.getOwnedByCode(code, ownerId), clock.instant());
    }

    // сценарий А — GET /links
    public LinkPage list(LinkListRequest request, UUID ownerId) {
        Instant now = clock.instant();
        int limit = request.limitOrDefault();

        List<LinkRow> rows = searchQuery.search(request, ownerId, now);
        boolean hasNextPage = rows.size() > limit;
        List<LinkRow> pageRows = hasNextPage ? rows.subList(0, limit) : rows;

        List<LinkListItem> items = toItems(pageRows, now);
        String nextCursor = hasNextPage ? cursorAfter(pageRows) : null;

        return new LinkPage(items, nextCursor);
    }

    private List<LinkListItem> toItems(List<LinkRow> rows, Instant now) {
        ClickTotals totals = statsFacade.getTotals(rows.stream().map(LinkRow::code).toList());
        return mapper.toListItems(rows, totals, now);
    }

    // последняя показанная строка
    private String cursorAfter(List<LinkRow> rows) {
        LinkRow last = rows.getLast();
        return new LinkCursor(last.createdAt(), last.code()).encode();
    }
}