package com.suman.newsfeed.domain.user.event;

import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import lombok.Getter;

@Getter
public class UserKeywordAddedEvent extends AbstractDomainEvent {

    private final String text;

    public UserKeywordAddedEvent(String userDomainId, String text) {
        super(userDomainId);
        this.text = text;
    }
}
