package com.suman.newsfeed.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Data;

import java.util.List;


@Data
public class SubscribeNewsPlatformRequest {

    @JsonProperty("news_platforms")
    private List<String> newsPlatform;
}
