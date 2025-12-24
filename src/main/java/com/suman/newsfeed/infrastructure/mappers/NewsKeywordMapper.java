package com.suman.newsfeed.infrastructure.mappers;

import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsKeywordJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsKeywordMapper {

    public NewsKeyword toDomain(NewsKeywordJpaEntity newsKeywordJpaEntity){
        return NewsKeyword.reconstruct(newsKeywordJpaEntity.getId(),
                newsKeywordJpaEntity.getDomainId(),
                newsKeywordJpaEntity.getText(),
                newsKeywordJpaEntity.getCollectedCount());

    }

    public NewsKeywordJpaEntity toEntity(NewsKeyword newsKeyword){
        return new NewsKeywordJpaEntity(
                newsKeyword.getId(),
                newsKeyword.getDomainId(),
                newsKeyword.getText(),
                newsKeyword.getCollectedCount()
        );
    }

    public List<NewsKeyword> toDomainList(List<NewsKeywordJpaEntity> newsKeywordJpaEntityList){
        return newsKeywordJpaEntityList.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }


}
