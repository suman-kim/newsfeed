package com.suman.newsfeed.infrastructure.database.jpa.entities;


import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsJpaEntity extends BaseEntity{

    @Column(columnDefinition = "LONGTEXT")
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String url;

    @Enumerated(EnumType.STRING)
    private NewsPlatform platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_keyword_id")
    private NewsKeywordJpaEntity newsKeyword;

    @Column(name = "image_url")
    private String imageUrl;


    public NewsJpaEntity(Long id, String domainId, String title, String content,String description, String url, NewsKeywordJpaEntity newsKeyword, NewsPlatform platform, String imageUrl) {
        this.id = id;
        this.domainId = domainId;
        this.title = title;
        this.content = content;
        this.description = description;
        this.newsKeyword = newsKeyword;
        this.url = url;
        this.platform = platform;
        this.imageUrl = imageUrl;
    }
}
