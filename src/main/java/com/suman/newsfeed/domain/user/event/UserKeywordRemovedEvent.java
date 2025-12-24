package com.suman.newsfeed.domain.user.event;


import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import lombok.Getter;

import java.util.List;

@Getter
public class UserKeywordRemovedEvent extends AbstractDomainEvent {
    private final List<String> textList;

    public UserKeywordRemovedEvent(String userDomainId, List<String> textList) {
        super(userDomainId);
        this.textList = textList;
    }
}
