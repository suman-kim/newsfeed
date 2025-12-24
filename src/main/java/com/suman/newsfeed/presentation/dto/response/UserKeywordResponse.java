package com.suman.newsfeed.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKeywordResponse {

    //사용자가 구독 중인 키워드
    private List<String> keywords;
}