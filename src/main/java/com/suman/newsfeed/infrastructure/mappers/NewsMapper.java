package com.suman.newsfeed.infrastructure.mappers;


import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsJpaEntity;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NewsMapper {

    private final NewsKeywordMapper newsKeywordMapper;

    public NewsJpaEntity toEntity(News news) {
        return new NewsJpaEntity(
                news.getId(),
                news.getDomainId(),
                news.getTitle(),
                news.getContent(),
                news.getDescription(),
                news.getUrl(),
                newsKeywordMapper.toEntity(news.getNewsKeyword()),
                news.getPlatform(),
                news.getImageUrl()
        );
    }

    public News toDomain(NewsJpaEntity newsJpaEntity){
        return News.reconstruct(newsJpaEntity.getId(),
                newsJpaEntity.getDomainId(),
                newsJpaEntity.getTitle(),
                newsJpaEntity.getContent(),
                newsJpaEntity.getDescription(),
                newsJpaEntity.getUrl(),
                newsKeywordMapper.toDomain(newsJpaEntity.getNewsKeyword()),
                newsJpaEntity.getPlatform(),
                newsJpaEntity.getImageUrl(),
                newsJpaEntity.getCreatedAt());
    }
}
