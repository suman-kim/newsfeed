package com.suman.newsfeed.infrastructure.external.crawler.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

// 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NaverNewsResponse {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<NaverNewsItem> items;
}
