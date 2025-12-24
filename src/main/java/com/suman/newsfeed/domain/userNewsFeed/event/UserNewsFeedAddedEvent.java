package com.suman.newsfeed.domain.userNewsFeed.event;

import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Getter;

@Getter
public class UserNewsFeedAddedEvent extends AbstractDomainEvent {

    private final String userDomainId;
    private final String title;
    private final String content;
    private final String url;
    private final NewsPlatform newsPlatform;
    private final Long keywordId;
    private final Boolean isRead;

    public UserNewsFeedAddedEvent(String domainId, String userDomainId, String title, String content, String url, NewsPlatform newsPlatform, Long keywordId, Boolean isRead) {
        super(domainId);
        this.userDomainId = userDomainId;
        this.title = title;
        this.content = content;
        this.url = url;
        this.newsPlatform = newsPlatform;
        this.keywordId = keywordId;
        this.isRead = isRead;
    }
}
