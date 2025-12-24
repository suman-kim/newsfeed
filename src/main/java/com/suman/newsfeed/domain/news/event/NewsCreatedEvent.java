package com.suman.newsfeed.domain.news.event;

import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Getter;

@Getter
public class NewsCreatedEvent extends AbstractDomainEvent {

    private final String title;
    private final String content;
    private final NewsPlatform platform;
    private final String imageUrl;

    public NewsCreatedEvent(String domainId, String title, String content, NewsPlatform platform, String imageUrl) {
        super(domainId);
        this.title = title;
        this.content = content;
        this.platform = platform;
        this.imageUrl = imageUrl;
    }

}
