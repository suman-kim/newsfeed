package com.suman.newsfeed.application.usecase;


import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddNewsKeywordUseCase {

    private final NewsKeywordRepository newsKeywordRepository;
    private final DomainEventPublisher domainEventPublisher;
    @Transactional
    public void execute(String text) {
        if (newsKeywordRepository.existsByText(text)) {
            return; // 이미 존재하면 skip
        }

        NewsKeyword keyword = NewsKeyword.create(text);
        newsKeywordRepository.save(keyword);

        // 저장 후 이벤트 발행
        domainEventPublisher.publishEvents(keyword);
    }

}
