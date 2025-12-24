package com.suman.newsfeed.domain.shared;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;



public abstract class AbstractDomainEvent implements DomainEvent {

    private final String eventId; // 이벤트 고유 ID
    private final String domainId; // 이벤트가 관련된 도메인 ID
    private final LocalDateTime occurredOn; // 이벤트가 발생한 시간
    private final Integer version; // 이벤트 버전 (기본값은 1)

    protected AbstractDomainEvent(String domainId) {
        this(domainId, 1);
    }

    protected AbstractDomainEvent(String domainId, Integer version) {
        this.eventId = UUID.randomUUID().toString();
        this.domainId = Objects.requireNonNull(domainId, "Aggregate ID는 필수입니다.");
        this.occurredOn = LocalDateTime.now();
        this.version = version;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDomainEvent that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', domainId='%s', occurredOn=%s, version=%d}",
                getClass().getSimpleName(), eventId, domainId, occurredOn, version);
    }
}
