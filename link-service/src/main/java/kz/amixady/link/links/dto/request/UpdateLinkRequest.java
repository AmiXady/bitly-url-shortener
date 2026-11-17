package kz.amixady.link.links.dto.request;

import java.util.List;

public record UpdateLinkRequest(
        String title,
        List<String> tags,
        String url
) {

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasTags() {
        return tags != null;
    }

    public boolean hasUrl() {
        return url != null;
    }

    public boolean isEmpty() {
        return !hasTitle() && !hasTags() && !hasUrl();
    }
}