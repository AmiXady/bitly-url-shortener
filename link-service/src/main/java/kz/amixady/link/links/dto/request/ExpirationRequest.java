package kz.amixady.link.links.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ExpirationRequest(
        @NotNull
        Instant expiresAt
) {
}