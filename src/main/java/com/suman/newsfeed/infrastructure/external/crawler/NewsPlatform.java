package com.suman.newsfeed.infrastructure.external.crawler;


import lombok.Getter;

import java.util.Arrays;

@Getter
public enum NewsPlatform {
    NAVER("네이버"),
    DAUM("다음"),
    GOOGLE("구글");

    private final String displayName;

    NewsPlatform(String displayName) {
        this.displayName = displayName;
    }

    // 문자열로부터 enum 찾기 (대소문자 무시)
    public static NewsPlatform fromString(String platform) {
        if (platform == null) return null;

        try {
            return NewsPlatform.valueOf(platform.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}