package kz.amixady.link.links.query;


import kz.amixady.link.links.exception.InvalidRequestException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public record LinkCursor(
        Instant createdAt,
        String code
) {
    private static final String SEPARATOR = "~";

    public String encode() {
        return createdAt + SEPARATOR + code;
    }

    public static LinkCursor decode(String value) {
        int i = value.indexOf(SEPARATOR);
        if (i <= 0 || i == value.length() - 1) {
            throw new InvalidRequestException("некорректный курсор");
        }
        try {
            return new LinkCursor(Instant.parse(value.substring(0, i)), value.substring(i + 1));
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException("некорректный курсор");
        }
    }
}