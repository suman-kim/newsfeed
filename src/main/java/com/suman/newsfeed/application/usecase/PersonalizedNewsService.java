package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.domain.user.*;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;

import com.suman.newsfeed.presentation.dto.response.PersonalizedNewsResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PersonalizedNewsService implements PersonalizedNewsUseCase {
    private static final Logger logger = LoggerFactory.getLogger(PersonalizedNewsService.class);

    private final NewsRepository newsRepository;
    private final NewsKeywordRepository newsKeywordRepository;
    private final UserRepository userRepository;

    @Override
    public List<PersonalizedNewsResponse> getPersonalizedNews(Long userId, int page, int size) {
        try {
            logger.info("사용자 {}의 개인화된 뉴스 조회 시작 - 페이지: {}, 크기: {}", userId, page, size);

            //사용자 조회
            User user = userRepository.findByUserIdWithKeywordsAndPlatforms(userId);
            //사용자 키워드
            Set<UserKeyword> userKeywords = user.getUserKeywords();
            //사용자 구독 플랫폼
            Set<UserNewsPlatform> userPlatforms = user.getUserNewsPlatforms();
            //키워드 text 추출
            Set<String> userKeywordTexts = userKeywords.stream()
                    .map(UserKeyword::getText)
                    .collect(Collectors.toSet());

            //뉴스 키워드 조회
            List<NewsKeyword> newsKeywordList = newsKeywordRepository.findAll();
            //해당하는 뉴스키워드만 추출
            Set<NewsKeyword> filteredNewsKeywords = newsKeywordList.stream()
                    .filter(newsKeyword -> userKeywordTexts.contains(newsKeyword.getText()))
                    .collect(Collectors.toSet());

            Set<NewsPlatform> newsPlatforms = userPlatforms.stream()
                    .map(UserNewsPlatform::getNewsPlatform)
                    .collect(Collectors.toSet());

            Pageable pageable = PageRequest.of(page, size);
            List<News> personalizedNews = newsRepository.findWithKeywordByKeywordsAndPlatforms(filteredNewsKeywords, newsPlatforms, pageable);

            List<PersonalizedNewsResponse> responses = personalizedNews.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            logger.info("personalizedNews {}", personalizedNews);

            logger.info("responses {}" ,responses);

            logger.info("사용자 {}의 개인화된 뉴스 조회 완료 - 결과 개수: {}", userId, responses.size());
            return responses;

        } catch (Exception e) {
            logger.error("사용자 {}의 개인화된 뉴스 조회 중 오류 발생", userId, e);
            throw new RuntimeException("개인화된 뉴스 조회 실패", e);
        }
    }


    private PersonalizedNewsResponse convertToResponse(News news) {

        return PersonalizedNewsResponse.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .description(news.getDescription())
                .url(news.getUrl())
                .platform(news.getPlatform())
                .matchedKeyword(news.getNewsKeyword().getText())
                .createdAt(news.getCreatedAt())
                .build();
    }
}