package com.suman.newsfeed.domain.news;

import java.util.List;


public interface NewsKeywordRepository {
    void save(NewsKeyword newsKeyword);
    List<String> findAllTextList();
    boolean existsByText(String text);
    List<NewsKeyword> findAll();
    void update(NewsKeyword newsKeyword);
    NewsKeyword findByText(String text);
    void deleteAllByTexts(List<String> textList);
}
