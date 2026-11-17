package kz.amixady.link.links.service;

import kz.amixady.link.links.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class LinkValidator {
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final Duration MIN_LIFETIME = Duration.ofMinutes(5);
    public static final int MAX_URL_LENGTH = 2048;
    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MAX_TAGS = 10;
    public static final int MAX_TAG_LENGTH = 50;
    private static final Pattern CUSTOM_CODE = Pattern.compile("^[A-Za-z0-9_-]{3,30}$");
    private final String ownHost;

    public LinkValidator(@Value("${app.own-host}") String ownHost) {
        this.ownHost = ownHost.toLowerCase(Locale.ROOT);
    }

    public void validateUrl(String url) {

    }

    public void validateTitle(String title) {
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidRequestException("заголовок — до " + MAX_TITLE_LENGTH + " символов");
        }
    }

    public void validateTags(List<String> tags) {
        if (tags == null) return;
        if (tags.size() > MAX_TAGS) {
            throw new InvalidRequestException("не больше " + MAX_TAGS + " тегов");
        }
        for (String tag : tags) {
            if (tag == null || tag.isBlank() || tag.length() > MAX_TAG_LENGTH) {
                throw new InvalidRequestException("тег — от 1 до " + MAX_TAG_LENGTH + " символов");
            }
        }
    }

    public void validateExpiresAt(Instant expiresAt, Instant now) {
        if (expiresAt != null && expiresAt.isBefore(now.plus(MIN_LIFETIME))) {
            throw new InvalidRequestException("invalid");
        }
    }

    public void validateCustomCode(String code) {

    }

}
