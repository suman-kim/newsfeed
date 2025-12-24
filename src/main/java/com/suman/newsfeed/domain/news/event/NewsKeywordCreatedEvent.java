package com.suman.newsfeed.domain.news.event;


import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import lombok.Getter;

@Getter
public class NewsKeywordCreatedEvent extends AbstractDomainEvent {
    private String text;

    public NewsKeywordCreatedEvent(String domainId, String text){
        super(domainId);
        this.text = text;
    }
}
