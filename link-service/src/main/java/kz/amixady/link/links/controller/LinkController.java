package kz.amixady.link.links.controller;

import jakarta.validation.Valid;
import kz.amixady.link.links.dto.LinkPage;
import kz.amixady.link.links.dto.request.*;
import kz.amixady.link.links.dto.response.LinkResponse;
import kz.amixady.link.links.dto.response.ShortenResult;
import kz.amixady.link.links.query.LinkQueryService;
import kz.amixady.link.links.query.LinkStatsQueryService;
import kz.amixady.link.links.usecase.ArchiveLinkUseCase;
import kz.amixady.link.links.usecase.DeleteLinkUseCase;
import kz.amixady.link.links.usecase.ShortenLinkUseCase;
import kz.amixady.link.links.usecase.UpdateLinkUseCase;
import kz.amixady.link.stats.api.LinkStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    // владелец; пока заголовок ставит клиент, позже — gateway после проверки JWT или API-ключа
    private static final String OWNER_HEADER = "X-User-Id";

    private final ShortenLinkUseCase shortenLink;
    private final UpdateLinkUseCase updateLink;
    private final ArchiveLinkUseCase archiveLink;
    private final DeleteLinkUseCase deleteLink;
    private final LinkQueryService linkQuery;
    private final LinkStatsQueryService statsQuery;

    // UC-08 — 201 создана, 200 возвращена существующая
    @PostMapping
    public ResponseEntity<LinkResponse> shorten(@Valid @RequestBody ShortenRequest request,
                                                @RequestHeader(OWNER_HEADER) UUID ownerId) {
        ShortenResult result = shortenLink.execute(request, ownerId);
        return ResponseEntity
                .status(result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.link());
    }

    // UC-11, А — список
    @GetMapping
    public LinkPage list(@Valid LinkListRequest request,
                         @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return linkQuery.list(request, ownerId);
    }

    // UC-11, Б — одна ссылка
    @GetMapping("/{code}")
    public LinkResponse get(@PathVariable String code,
                            @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return linkQuery.getByCode(code, ownerId);
    }

    // UC-15 — статистика
    @GetMapping("/{code}/stats")
    public LinkStatsResponse stats(@PathVariable String code,
                                   @Valid LinkStatsRequest request,
                                   @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return statsQuery.getStats(code, request, ownerId);
    }

    // UC-12, А — заголовок, теги, адрес
    @PatchMapping("/{code}")
    public LinkResponse update(@PathVariable String code,
                               @RequestBody UpdateLinkRequest request,
                               @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return updateLink.update(code, request, ownerId);
    }

    // UC-12, Б — задать или перенести срок жизни
    @PutMapping("/{code}/expiration")
    public LinkResponse changeExpiration(@PathVariable String code,
                                         @Valid @RequestBody ExpirationRequest request,
                                         @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return updateLink.changeExpiration(code, request, ownerId);
    }

    // UC-12, Б — сделать бессрочной
    @DeleteMapping("/{code}/expiration")
    public LinkResponse removeExpiration(@PathVariable String code,
                                         @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return updateLink.removeExpiration(code, ownerId);
    }

    // UC-13 — в архив
    @PostMapping("/{code}/archive")
    public LinkResponse archive(@PathVariable String code,
                                @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return archiveLink.archive(code, ownerId);
    }

    // UC-13 — из архива
    @DeleteMapping("/{code}/archive")
    public LinkResponse unarchive(@PathVariable String code,
                                  @RequestHeader(OWNER_HEADER) UUID ownerId) {
        return archiveLink.unarchive(code, ownerId);
    }

    // UC-14 — удалить, 204 и при повторе
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code,
                       @RequestHeader(OWNER_HEADER) UUID ownerId) {
        deleteLink.execute(code, ownerId);
    }
}