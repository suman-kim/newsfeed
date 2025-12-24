package com.suman.newsfeed.domain.news;

import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface NewsRepository {

    Long save(News news);
    List<News> findWithKeywordByKeywordsAndPlatforms(Set<NewsKeyword> keywords, Set<NewsPlatform> platforms, Pageable pageable);


}
