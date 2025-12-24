package com.suman.newsfeed.infrastructure.database.jpa.entities;


import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "user_news_platforms")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserNewsPlatformEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NewsPlatform newsPlatform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    //기본 생성자
    public UserNewsPlatformEntity(Long id, NewsPlatform newsPlatform, UserJpaEntity user) {
        this.id = id;
        this.newsPlatform = newsPlatform;
        this.user = user;
    }
}
