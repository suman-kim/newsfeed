package com.suman.newsfeed.domain.news;


import com.suman.newsfeed.domain.news.event.NewsCreatedEvent;
import com.suman.newsfeed.domain.shared.BaseAggregate;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class News extends BaseAggregate {
    private Long id;
    private String title;
    private String content;
    private String description;
    private String url;
    private NewsPlatform platform;
    private String imageUrl;
    private NewsKeyword newsKeyword;
    private LocalDateTime createdAt;


    private News(Long id, String domainId, String title, String content, String description, String url, NewsKeyword newsKeyword, NewsPlatform platform, String imageUrl, LocalDateTime createdAt) {
        super(domainId);
        this.id = id;
        this.title = title;
        this.content = content;
        this.description = description;
        this.url = url;
        this.newsKeyword = newsKeyword;
        this.platform = platform;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public static News create(String title, String content, String description, String url, NewsKeyword newsKeyword, NewsPlatform platform, String imageUrl) {
        String domainId = UUID.randomUUID().toString();
        News news = new News(null, domainId, title, content,description, url, newsKeyword,platform, imageUrl, LocalDateTime.now());
        //도메인 이벤트 생성
        news.addDomainEvent(new NewsCreatedEvent(
                domainId,
                news.title,
                news.content,
                news.platform,
                news.imageUrl
        ));

        return news;
    }

    // 매퍼에서 사용
    public static News reconstruct(Long id, String domainId, String title, String content,String description, String url,NewsKeyword newsKeyword, NewsPlatform platform, String imageUrl, LocalDateTime createdAt) {
            return new News(id, domainId, title, content, description,url, newsKeyword, platform, imageUrl, createdAt);
    }

}
