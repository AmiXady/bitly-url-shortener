package kz.amixady.link.links.usecase;


import kz.amixady.link.links.dto.response.LinkResponse;
import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.mapper.LinkMapper;
import kz.amixady.link.links.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class ArchiveLinkUseCase {
    private final LinkRepository repository;
    private final LinkMapper linkMapper;
    private final Clock clock;


    public LinkResponse archive(String code, UUID ownerId) {
        Link link = repository.getOwnedByCode(code, ownerId);
        link.archive();
        return linkMapper.toResponse(link, clock.instant());
    }

    public LinkResponse unarchive(String code, UUID ownerId) {
        Link link = repository.getOwnedByCode(code, ownerId);
        link.unarchive();
        return linkMapper.toResponse(link, clock.instant());
    }
}
