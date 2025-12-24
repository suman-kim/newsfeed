package com.suman.newsfeed.domain.user.event;

import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import lombok.Getter;

@Getter
public class UserKeywordActiveEvent extends AbstractDomainEvent {

    private final String text;

    public UserKeywordActiveEvent(String userDomainId, String text) {
        super(userDomainId);
        this.text = text;
    }
}