package com.suman.newsfeed.domain.shared;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public abstract class BaseAggregate {

    protected final String domainId;

    // 도메인 이벤트 저장소
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // 도메인 이벤트 추가
    protected void addDomainEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("도메인 이벤트는 null일 수 없습니다.");
        }
        this.domainEvents.add(event);
    }

    // 도메인 이벤트 조회 (불변 리스트 반환)
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // 도메인 이벤트 클리어 (이벤트 발행 후 호출)
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    //도메인 이벤트 존재 여부 확인
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }
}
