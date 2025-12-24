package com.suman.newsfeed.application.event;

import com.suman.newsfeed.application.usecase.AddNewsKeywordUseCase;
import com.suman.newsfeed.application.usecase.NewsCollectionUseCase;
import com.suman.newsfeed.application.usecase.RemoveNewsKeywordUseCase;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.domain.news.event.NewsKeywordCreatedEvent;
import com.suman.newsfeed.domain.shared.DomainEvent;
import com.suman.newsfeed.domain.user.event.UserKeywordActiveEvent;
import com.suman.newsfeed.domain.user.event.UserKeywordAddedEvent;
import com.suman.newsfeed.domain.user.event.UserKeywordRemovedEvent;
import com.suman.newsfeed.domain.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventHandler {

    private final NewsCollectionUseCase newsCollectionUseCase;
    private final AddNewsKeywordUseCase addNewsKeywordUseCase;
    private final RemoveNewsKeywordUseCase removeNewsKeywordUseCase;

    //모든 도메인 이벤트 핸들러
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleAllDomainEvents(DomainEvent domainEvent){
        log.info("공통 도메인 이벤트 수신: {}", domainEvent);
        //추후 메트릭 수집
    }


    // 사용자 가입 이벤트 핸들러
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("사용자 등록 이벤트 수신: {}", event);
        log.info("사용자 등록 이벤트 ID", event.getEventId());
        log.info("사용자 등록 이벤트 domain ID: {}", event.getDomainId());

    }


    // 사용자 키워드 생성 이벤트 핸들러
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleUserKeywordAdded(UserKeywordAddedEvent event) {
        log.info("사용자 키워드 추가 이벤트 수신: {}", event);
        log.info("사용자 키워드 추가 이벤트 ID", event.getEventId());
        log.info("사용자 키워드 추가 이벤트 domain ID: {}", event.getDomainId());
        log.info("사용자 키워드 추가 이벤트 키워드: {}", event.getText());

        //뉴스 키워드 생성 호출
        addNewsKeywordUseCase.execute(event.getText());
    }

    //사용자 키워드 삭제 이벤트 핸들러
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleUserKeywordRemoved(UserKeywordRemovedEvent event) {
        log.info("사용자 키워드 삭제 이벤트 수신: {}", event);
        log.info("사용자 키워드 삭제 이벤트 ID", event.getEventId());
        log.info("사용자 키워드 삭제 이벤트 domain ID: {}", event.getDomainId());
        log.info("사용자 키워드 삭제 이벤트 키워드: {}", event.getTextList());

        //뉴스 키워드 삭제 호출
        removeNewsKeywordUseCase.execute(event.getTextList());

    }

    //사용자 키워드 활성화/비활성 이벤트 핸들러
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleUserKeywordActive(UserKeywordActiveEvent event) {
        log.info("사용자 키워드 활성화/비활성 이벤트 수신: {}", event);
        log.info("사용자 키워드 활성화/비활성 이벤트 ID", event.getEventId());
        log.info("사용자 키워드 활성화/비활성 이벤트 domain ID: {}", event.getDomainId());
        log.info("사용자 키워드 활성화/비활성 이벤트 키워드: {}", event.getText());

    }

    //뉴스 키워드 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("eventExecutor")
    public void handleNewsKeywordCreated(NewsKeywordCreatedEvent event) {

        log.info("뉴스 키워드 생성 이벤트 수신: {}", event);
        log.info("뉴스 키워드 생성 이벤트 ID: {}", event.getEventId());
        log.info("뉴스 키워드 생성 이벤트 domain ID: {}", event.getDomainId());
        log.info("뉴스 키워드 생성 이벤트 텍스트: {}", event.getText());
        //TODO: 키워드 추가 시 뉴스피드 생성 로직 필요
        newsCollectionUseCase.collectNewsForKeyword(event.getText());
    }



}