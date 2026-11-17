package kz.amixady.link.links.dto;

import java.util.List;

public record LinkPage(
        List<LinkListItem> items,
        String nextCursor
) {

    public LinkPage {
        items = List.copyOf(items);
    }

    public boolean hasMore() {
        return nextCursor != null;
    }
}