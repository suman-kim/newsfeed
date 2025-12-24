package com.suman.newsfeed.infrastructure.external.crawler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class NewsDataProviderAdapter implements NewsDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(NewsDataProviderAdapter.class);

    private final List<CrawlerStrategy> crawlerStrategies;
    private final Executor taskExecutor;

    @Override
    public List<NewsDataDto> fetchNewsByKeyword(String keyword, Long pageNumber,int pageSize) {
        // 사용자가 구독한 플랫폼만 필터링
        List<CrawlerStrategy> enabledAndSubscribedStrategies = crawlerStrategies.stream()
                .filter(CrawlerStrategy::isEnabled) // 전체적으로 활성화된 것
                .toList();

        if (enabledAndSubscribedStrategies.isEmpty()) {
            logger.warn("활성화된 크롤러 전략이 없습니다. 키워드: {}", keyword);
            return List.of();
        }

        logger.info("키워드 '{}' 크롤링 대상 플랫폼: {}", keyword,
                enabledAndSubscribedStrategies.stream()
                        .map(strategy -> strategy.getPlatform().getDisplayName())
                        .toList());

        //플랫폼별로 병렬 크롤링
        List<CompletableFuture<List<NewsDataDto>>> futures = enabledAndSubscribedStrategies.stream()
                .map(strategy -> CompletableFuture.supplyAsync(() -> {
                    try {
                        logger.info("크롤링 시작: {} - {} (페이지: {})", strategy.getPlatform().getDisplayName(), keyword, pageNumber);

                        List<NewsDataDto> newsDataDtoList = strategy.crawlNews(keyword, pageNumber,pageSize);

                        logger.info("크롤링 완료: {} - {}개 뉴스 (페이지: {})",
                                strategy.getPlatform().getDisplayName(), newsDataDtoList.size(), pageNumber);

                        return newsDataDtoList;
                    } catch (Exception e) {
                        logger.error("크롤링 실패 [{}] 키워드: {}, 페이지: {}, 오류: {}",
                                strategy.getPlatform().getDisplayName(), keyword, pageNumber, e.getMessage());
                        return List.<NewsDataDto>of();
                    }
                }, taskExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<NewsPlatform> getSupportedPlatforms() {
        return crawlerStrategies.stream()
                .filter(CrawlerStrategy::isEnabled)
                .map(CrawlerStrategy::getPlatform)
                .collect(Collectors.toList());
    }
}