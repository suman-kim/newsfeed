package com.suman.newsfeed.infrastructure.external.crawler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class NewsDataDto {
    private String title;
    private String content;
    private String description;
    private String url;
    private String keyword;
    private NewsPlatform platform;
    private LocalDateTime crawledAt;

    public NewsDataDto(String title, String content, String description, String url,String keyword, NewsPlatform platform) {
        this.title = title;
        this.content = content;
        this.description = description;
        this.url = url;
        this.keyword = keyword;
        this.platform = platform;
        this.crawledAt = LocalDateTime.now();
    }

}
