package com.suman.newsfeed.infrastructure.external.crawler;

import java.util.List;

public interface NewsDataProvider {
    List<NewsDataDto> fetchNewsByKeyword(String keyword,Long pageNumber,int pageSize);
    List<NewsPlatform> getSupportedPlatforms();
}
