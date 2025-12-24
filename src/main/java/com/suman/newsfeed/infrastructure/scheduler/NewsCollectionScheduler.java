package com.suman.newsfeed.infrastructure.scheduler;



import com.suman.newsfeed.application.usecase.NewsCollectionUseCase;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class NewsCollectionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsCollectionScheduler.class);

    private final NewsCollectionUseCase newsCollectionUseCase;

    // 매시 정각마다 실행
    @Scheduled(initialDelay = 3000, fixedRate = 3600000)
//    @Scheduled(initialDelay = 3000, fixedRate = 60000)
    public void scheduledNewsCollection() {
        executeNewsCollection();
    }

    // 공통 실행 메서드
    private void executeNewsCollection() {
        logger.info("뉴스 수집 시작: {}", LocalDateTime.now());

        try {
            newsCollectionUseCase.collectNewsAll();

        } catch (Exception e) {
            logger.error(" 뉴스 수집 실패: {}", e.getMessage(), e);
        }

        logger.info(" 뉴스 수집 종료: {}", LocalDateTime.now());
    }
}