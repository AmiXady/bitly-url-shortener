package kz.amixady.link.links.usecase;


import kz.amixady.link.links.entity.Link;
import kz.amixady.link.links.exception.LinkNotFoundException;
import kz.amixady.link.links.repository.LinkRepository;
import kz.amixady.link.stats.StatsFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteLinkUseCase {

    private final LinkRepository repository;
    private final StatsFacade statsFacade;

    @Transactional
    public void execute(String code, UUID ownerId){
        Link link = repository.findByCodeAndOwnerId(code, ownerId)
                .orElseThrow(() -> new LinkNotFoundException(code));
        link.delete();
        statsFacade.deleteStats(code);
    }
}
