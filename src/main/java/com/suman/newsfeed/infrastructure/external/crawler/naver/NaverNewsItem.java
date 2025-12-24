package com.suman.newsfeed.infrastructure.external.crawler.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NaverNewsItem {
    private String title;
    private String description;
    private String originallink;
    private String link;
    private String pubDate;
}
