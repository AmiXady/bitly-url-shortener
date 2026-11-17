package kz.amixady.link.links.dto.response;

import kz.amixady.link.links.entity.LinkStatus;

import java.time.Instant;

public enum LinkDisplayStatus {
    ACTIVE, ARCHIVED, EXPIRED;

    public static LinkDisplayStatus of(LinkStatus status, Instant expiresAt, Instant now) {
        if (status == LinkStatus.ARCHIVED) {
            return ARCHIVED;
        }
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            return EXPIRED;
        }
        return ACTIVE;
    }
}