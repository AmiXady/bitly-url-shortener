package kz.amixady.link.links.usecase;

import kz.amixady.link.links.dto.request.ExpirationRequest;
import kz.amixady.link.links.dto.response.LinkResponse;
import kz.amixady.link.links.dto.request.UpdateLinkRequest;
import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.exception.InvalidRequestException;
import kz.amixady.link.links.mapper.LinkMapper;
import kz.amixady.link.links.repository.LinkRepository;
import kz.amixady.link.links.service.LinkValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * UC-12 Изменить ссылку — 00-docs/02-use-cases/uc/uc-12.md
 */
@Component
@Transactional
@RequiredArgsConstructor
public class UpdateLinkUseCase {

    private final LinkRepository repository;
    private final LinkValidator validator;
    private final LinkMapper mapper;
    private final Clock clock;

    // сценарий А — PATCH /links/{code}
    public LinkResponse update(String code, UpdateLinkRequest request, UUID ownerId) {
        Instant now = clock.instant();
        validate(request);
        Link link = repository.getOwnedByCode(code, ownerId);
        link.update(request.title(), request.tags(), request.url(), now);
        return mapper.toResponse(link, now);
    }

    // сценарий Б — PUT /links/{code}/expiration
    public LinkResponse changeExpiration(String code, ExpirationRequest request, UUID ownerId) {
        Instant now = clock.instant();
        validator.validateExpiresAt(request.expiresAt(), now);
        Link link = repository.getOwnedByCode(code, ownerId);
        link.changeExpiration(request.expiresAt());
        return mapper.toResponse(link, now);
    }

    // сценарий Б — DELETE /links/{code}/expiration
    public LinkResponse removeExpiration(String code, UUID ownerId) {
        Link link = repository.getOwnedByCode(code, ownerId);
        link.removeExpiration();
        return mapper.toResponse(link, clock.instant());
    }

    private void validate(UpdateLinkRequest request) {
        if (request.isEmpty()) {
            throw new InvalidRequestException("нужно передать хотя бы одно поле: title, tags или url");
        }
        if (request.hasUrl()) {
            validator.validateUrl(request.url());
        }
        validator.validateTitle(request.title());
        validator.validateTags(request.tags());
    }
}