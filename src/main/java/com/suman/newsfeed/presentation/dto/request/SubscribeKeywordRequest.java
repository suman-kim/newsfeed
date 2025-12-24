package com.suman.newsfeed.presentation.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SubscribeKeywordRequest {
    private List<String> keywords;
}