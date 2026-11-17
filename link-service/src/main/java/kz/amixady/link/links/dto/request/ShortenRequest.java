package kz.amixady.link.links.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record ShortenRequest(
        @NotBlank
        String url,
        String code,
        String title,
        List<String> tags,
        Instant expiresAt
) {

    public boolean hasCode() {
        return code != null && !code.isBlank();
    }

    public boolean hasExpiresAt() {
        return expiresAt != null;
    }
}