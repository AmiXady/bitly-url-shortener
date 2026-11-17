package kz.amixady.link.links.dto.response;

import kz.amixady.link.links.entity.SafetyStatus;

import java.time.Instant;
import java.util.List;

public record LinkResponse(
        String shortUrl,
        String code,
        String url,
        String pendingUrl,
        String pendingUrlRejection,
        String title,
        List<String> tags,
        Instant expiresAt,
        LinkDisplayStatus status,
        SafetyStatus safetyStatus,
        Instant createdAt
) {
}