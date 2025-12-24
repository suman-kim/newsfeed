package com.suman.newsfeed.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedNewsResponse {

    @JsonProperty("news_id")
    private Long newsId;
    private String title;
    private String content;
    private String description;
    private String url;
    private NewsPlatform platform;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("matched_keyword")
    private String matchedKeyword; // 매칭된 키워드
}