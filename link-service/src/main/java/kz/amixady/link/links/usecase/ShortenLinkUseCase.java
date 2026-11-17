package kz.amixady.link.links.usecase;

import kz.amixady.link.links.dto.request.ShortenRequest;
import kz.amixady.link.links.dto.response.ShortenResult;
import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.exception.CodeTakenException;
import kz.amixady.link.links.mapper.LinkMapper;
import kz.amixady.link.links.repository.LinkRepository;
import kz.amixady.link.links.service.LinkValidator;
import kz.amixady.link.links.service.code.CodeGenerator;
import kz.amixady.link.links.service.quota.DailyLinkQuota;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-08 Сократить ссылку.
 *
 * @see <a href="../../../../../../../../00-docs/02-use-cases/uc/uc-08.md">UC-08</a>
 */
@Component
@RequiredArgsConstructor
public class ShortenLinkUseCase {

    private static final int MAX_CODE_ATTEMPTS = 5;

    private final LinkRepository repository;
    private final LinkMapper mapper;
    private final LinkValidator validator;
    private final DailyLinkQuota quota;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public ShortenResult execute(ShortenRequest request, UUID ownerId) {
        Instant now = clock.instant();

        validate(request, now);

        // шаг 5 и 4d: дедупликация и идемпотентность — возвращаем существующую, 200
        Optional<Link> existing = findExisting(request, ownerId, now);
        if (existing.isPresent()) {
            return ShortenResult.existing(mapper.toResponse(existing.get(), now));
        }

        // шаги 6–10: квота, код, сохранение — 201
        Link created = createWithinQuota(request, ownerId, now);
        return ShortenResult.created(mapper.toResponse(created, now));
    }

    private void validate(ShortenRequest request, Instant now) {
        validator.validateUrl(request.url());
        validator.validateTitle(request.title());
        validator.validateTags(request.tags());
        validator.validateExpiresAt(request.expiresAt(), now);
        if (request.hasCode()) {
            validator.validateCustomCode(request.code());
        }
    }

    private Optional<Link> findExisting(ShortenRequest request, UUID ownerId, Instant now) {
        // шаг 5, 5a — дедупликация: действующая сгенерированная ссылка на тот же URL (F-3.5)
        if (!request.hasCode()) {
            return repository.findReusable(ownerId, request.url(), now);
        }

        // 4c–4e — свой код: свободен → создаём, повтор того же запроса → 200 (идемпотентность), иначе 409 (F-3.16)
        Optional<Link> found = repository.findByCode(request.code());
        if (found.isPresent() && !found.get().canBeReusedFor(ownerId, request.url(), now)) {
            throw new CodeTakenException(request.code());
        }
        return found;
    }

    // шаг 6, 6a — квота тратится только на реально созданную ссылку (F-12.9)
    private Link createWithinQuota(ShortenRequest request, UUID ownerId, Instant now) {
        quota.consume(ownerId, now);
        try {
            return save(request, ownerId, now);
        } catch (RuntimeException e) {
            quota.release(ownerId, now);
            throw e;
        }
    }

    // шаг 8, 8a — уникальный индекс на code — финальная защита от гонки
    private Link save(ShortenRequest request, UUID ownerId, Instant now) {
        String code = resolveCode(request);
        try {
            return repository.saveAndFlush(Link.create(
                    code,
                    request.hasCode(),
                    request.url(),
                    ownerId,
                    request.title(),
                    request.tags(),
                    request.expiresAt(),
                    now));
        } catch (DataIntegrityViolationException e) {
            throw new CodeTakenException(code);
        }
    }

    // шаг 7 — свой код или сгенерированный
    private String resolveCode(ShortenRequest request) {
        return request.hasCode() ? request.code() : generateFreeCode();
    }

    // 7a — коллизия сгенерированного кода: тихо пробуем ещё раз
    private String generateFreeCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (!repository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate a free code in " + MAX_CODE_ATTEMPTS + " attempts");
    }
}