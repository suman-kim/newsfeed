package com.suman.newsfeed.infrastructure.external.crawler;

import com.suman.newsfeed.domain.news.News;

import java.util.List;

public interface CrawlerStrategy {
    NewsPlatform getPlatform();
    List<NewsDataDto> crawlNews(String keyword,Long pageNumber,int pageSize);
    boolean isEnabled();

    default String getPlatformName() {
        return getPlatform().getDisplayName();
    }
}