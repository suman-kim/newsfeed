package com.suman.newsfeed.application.usecase;


import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.domain.user.*;

import com.suman.newsfeed.infrastructure.external.crawler.NewsDataDto;
import com.suman.newsfeed.infrastructure.external.crawler.NewsDataProvider;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@RequiredArgsConstructor
@Service
public class NewsCollectionService implements NewsCollectionUseCase {

    private static final Logger logger = LoggerFactory.getLogger(NewsCollectionService.class);
    private final NewsKeywordRepository newsKeywordRepository;
    private final NewsRepository newsRepository;
    private final NewsDataProvider newsDataProvider;
    private final Executor taskExecutor; // 스레드 풀 주입
    private static final int PageSize = 10;

    @Override
    public CompletableFuture<Void> collectNewsAll() {
        try {
            //뉴스 키워드 조회
            List<NewsKeyword> newsKeywordList = newsKeywordRepository.findAll();

            logger.info("News Keyword List: {}", newsKeywordList);

            Set<NewsKeyword> newsKeywords = new HashSet<>(newsKeywordList);

            List<NewsPlatform> newsPlatformList = newsDataProvider.getSupportedPlatforms();
            if (newsPlatformList.isEmpty()) {
                logger.info("활성화된 뉴스 플랫폼이 없습니다.");
                return CompletableFuture.completedFuture(null);
            }

            // 키워드별로 뉴스를 한 번만 수집
            List<CompletableFuture<Void>> keywordFutures = newsKeywords.stream()
                    .map(this::collectNewsForKeywordAsync)
                    .toList();

            return CompletableFuture.allOf(keywordFutures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> logger.info("모든 키워드에 대한 뉴스 수집 완료"));

        } catch (Exception e) {
            logger.error("뉴스 수집 서비스 초기화 중 오류 발생", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    //키워드 구독시 즉시 뉴스 수집
    @Override
    public CompletableFuture<Void> collectNewsForKeyword(String keyword) {
        try {
            //키워드 조회
            NewsKeyword newsKeyword = newsKeywordRepository.findByText(keyword);

            if (newsKeyword == null) {
                logger.warn("키워드 '{}'를 찾을 수 없습니다.", keyword);
                return CompletableFuture.completedFuture(null);
            }

            logger.info("키워드 '{}'에 대한 뉴스 수집 시작", keyword);

            return collectNewsForKeywordAsync(newsKeyword);

        } catch (Exception e) {
            logger.error("키워드 '{}'에 대한 뉴스 수집 중 오류 발생: {}", keyword, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    // 공통 로직을 담은 비동기 뉴스 수집 메소드
    private CompletableFuture<Void> collectNewsForKeywordAsync(NewsKeyword newsKeyword) {
        return CompletableFuture.runAsync(() -> {
            String text = newsKeyword.getText();
            Long pageNumber = newsKeyword.getCollectedCount();


            try {
                logger.info("키워드 '{}' 뉴스 수집 시작 - 페이지 번호 {}번", text, pageNumber);

                List<NewsDataDto> newsDataDtoList = newsDataProvider.fetchNewsByKeyword(text, pageNumber, PageSize);

                if (newsDataDtoList.isEmpty()) {
                    logger.info("키워드 '{}'에 대한 뉴스가 없습니다.", text);
                    return;
                }

                updateNewsKeyword(newsKeyword);
                createNews(newsKeyword,newsDataDtoList);

                logger.info("키워드 '{}'에 대한 뉴스 수집 완료 - 뉴스 개수: {}", text, newsDataDtoList.size());

            } catch (Exception e) {
                logger.error("키워드 '{}' 처리 중 오류 발생", text, e);
                throw new RuntimeException("뉴스 수집 실패", e);
            }
        }, taskExecutor);
    }


    @Transactional
    protected void createNews(NewsKeyword newsKeyword, List<NewsDataDto> newsDataDtoList){
        try{

            logger.info("뉴스 데이터 {}", newsDataDtoList);

            newsDataDtoList.forEach(newsDataDto -> {

                News news = News.create(newsDataDto.getTitle(),
                                        newsDataDto.getContent(),
                                        newsDataDto.getDescription(),
                                        newsDataDto.getUrl(),
                                        newsKeyword,
                                        newsDataDto.getPlatform(),
                                        null);
                newsRepository.save(news);
            });
        }
        catch (Exception e){
            logger.error("뉴스 데이터 생성 중 오류 발생", e);
        }
    }

    @Transactional
    protected  void updateNewsKeyword(NewsKeyword newsKeyword){
        logger.info("뉴스 키워드 카운트 업데이트 -> {}",newsKeyword);
        newsKeyword.updateCount();
        newsKeywordRepository.update(newsKeyword);
    }
}
