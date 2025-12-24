package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsKeywordJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.NewsJpaRepository;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import com.suman.newsfeed.infrastructure.mappers.NewsKeywordMapper;
import com.suman.newsfeed.infrastructure.mappers.NewsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NewsRepositoryAdapter implements NewsRepository {
    private static final Logger logger = LoggerFactory.getLogger(NewsRepositoryAdapter.class);
    private final NewsJpaRepository  newsJpaRepository;
    private final NewsMapper newsMapper;
    private final NewsKeywordMapper newsKeywordMapper;
    @Override
    public Long save(News news) {
        NewsJpaEntity newsJpaEntity = newsJpaRepository.save(newsMapper.toEntity(news));
        return newsJpaEntity.getId();
    }

    @Override
    public List<News> findWithKeywordByKeywordsAndPlatforms(Set<NewsKeyword> keywords, Set<NewsPlatform> platforms, Pageable pageable) {
        logger.info("keywords {}", keywords);
        logger.info("platforms {}", platforms);

        // 1. 도메인 객체(NewsKeyword) Set을 JPA 엔티티(NewsKeywordJpaEntity) Set으로 변환합니다.
        //    (NewsMapper에 toKeywordEntity와 같은 변환 메서드가 정의되어 있다고 가정합니다.)
        Set<NewsKeywordJpaEntity> keywordEntities = keywords.stream()
                .map(newsKeywordMapper::toEntity)
                .collect(Collectors.toSet());

        // 2. JpaRepository에 정의한 쿼리 메서드를 호출하여 데이터를 조회합니다.
        Page<NewsJpaEntity> newsEntityPage = newsJpaRepository.findWithKeywordByKeywordsAndPlatforms(keywordEntities, platforms, pageable);

        // 3. 조회된 Page 객체에서 콘텐츠(List<NewsJpaEntity>)를 가져와
        //    도메인 객체(List<News>) 리스트로 변환하여 반환합니다.
        return newsEntityPage.getContent().stream()
                .map(newsMapper::toDomain)
                .collect(Collectors.toList());
    }


}
