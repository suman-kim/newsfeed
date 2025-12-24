package com.suman.newsfeed.application.event;

import com.suman.newsfeed.domain.shared.BaseAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public <T extends BaseAggregate> void publishEvents(T aggregate) {
        log.info("도메인 이벤트 발행 시작: {}");
        if (aggregate.hasDomainEvents()) {
            aggregate.getDomainEvents().forEach(event -> {
                log.info("도메인 이벤트 발행: {}", event);
                applicationEventPublisher.publishEvent(event);
            });
            aggregate.clearDomainEvents();
        }
    }
}