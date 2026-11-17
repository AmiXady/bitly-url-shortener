package kz.amixady.link.links.dto;

import kz.amixady.link.links.dto.response.LinkDisplayStatus;
import kz.amixady.link.links.entity.SafetyStatus;

import java.time.Instant;
import java.util.List;

public record LinkListItem(
        String shortUrl,
        String code,
        String title,
        String url,
        List<String> tags,
        Instant createdAt,
        LinkDisplayStatus status,
        SafetyStatus safetyStatus,
        long clicks
) {
}