package com.suman.newsfeed.domain.user.event;

import com.suman.newsfeed.domain.shared.AbstractDomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserRegisteredEvent extends AbstractDomainEvent {

    private final String email;
    private final String nickname;
    private final LocalDateTime registeredAt;

    public UserRegisteredEvent(String userDomainId, String email, String nickname) {
        super(userDomainId);
        this.email = email;
        this.nickname = nickname;
        this.registeredAt = getOccurredOn(); // 발생 시간과 동일
    }
}


