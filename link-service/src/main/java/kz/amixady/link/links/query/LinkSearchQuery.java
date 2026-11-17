package kz.amixady.link.links.query;

import kz.amixady.link.links.dto.request.LinkListRequest;
import kz.amixady.link.links.entity.LinkStatus;
import kz.amixady.link.links.entity.SafetyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class LinkSearchQuery {

    private final NamedParameterJdbcTemplate jdbc;

    public List<LinkRow> search(LinkListRequest request, UUID ownerId, Instant now) {
        StringBuilder sql = new StringBuilder("""
                SELECT code, title, url, tags, created_at, expires_at, status, safety_status
                FROM links
                WHERE owner_id = :ownerId
                  AND status <> 'DELETED'
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ownerId", ownerId)
                .addValue("now", ts(now));

        if (request.hasSearch()) {
            sql.append(" AND lower(code) LIKE :search ESCAPE '\\'");
            params.addValue("search", "%" + escapeLike(request.search().toLowerCase(Locale.ROOT)) + "%");
        }

        if (request.hasTag()) {
            sql.append(" AND tags @> ARRAY[:tag]::text[]");
            params.addValue("tag", request.tag());
        }

        if (request.hasCreatedFrom()) {
            sql.append(" AND created_at >= :createdFrom");
            params.addValue("createdFrom", ts(request.createdFrom()));
        }

        if (request.hasCreatedTo()) {
            sql.append(" AND created_at < :createdTo");
            params.addValue("createdTo", ts(request.createdTo()));
        }

        switch (request.status()) {
            case null -> {
                sql.append(" AND status <> :status");
                params.addValue("status", LinkStatus.ARCHIVED.getValue());
            }
            case ACTIVE -> {
                sql.append(" AND status = :status AND (expires_at IS NULL OR expires_at > :now)");
                params.addValue("status", LinkStatus.ACTIVE.getValue());
            }
            case ARCHIVED -> {
                sql.append(" AND status = :status");
                params.addValue("status", LinkStatus.ARCHIVED.getValue());
            }
            case EXPIRED -> {
                sql.append(" AND status = :status AND expires_at <= :now");
                params.addValue("status", LinkStatus.ACTIVE.getValue());
            }
        }

        if (request.hasCursor()) {
            LinkCursor cursor = LinkCursor.decode(request.cursor());
            sql.append(" AND (created_at, code) < (:cursorCreatedAt, :cursorCode)");
            params.addValue("cursorCreatedAt", ts(cursor.createdAt()))
                    .addValue("cursorCode", cursor.code());
        }

        sql.append(" ORDER BY created_at DESC, code DESC LIMIT :limit");
        params.addValue("limit", request.limitOrDefault() + 1);

        return jdbc.query(sql.toString(), params, ROW_MAPPER);
    }

    private static final RowMapper<LinkRow> ROW_MAPPER = (rs, rowNum) -> new LinkRow(
            rs.getString("code"),
            rs.getString("title"),
            rs.getString("url"),
            toList(rs.getArray("tags")),
            rs.getTimestamp("created_at").toInstant(),
            toInstant(rs.getTimestamp("expires_at")),
            LinkStatus.valueOf(rs.getString("status")),
            SafetyStatus.valueOf(rs.getString("safety_status"))
    );

    private static List<String> toList(Array array) throws java.sql.SQLException {
        return array == null ? List.of() : List.of((String[]) array.getArray());
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Timestamp ts(Instant instant) {
        return Timestamp.from(instant);
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}