package kz.amixady.link.links.query;

import kz.amixady.link.links.entity.LinkStatus;
import kz.amixady.link.links.entity.SafetyStatus;

import java.time.Instant;
import java.util.List;

public record LinkRow(
        String code,
        String title,
        String url,
        List<String> tags,
        Instant createdAt,
        Instant expiresAt,
        LinkStatus status,
        SafetyStatus safetyStatus
) {
}