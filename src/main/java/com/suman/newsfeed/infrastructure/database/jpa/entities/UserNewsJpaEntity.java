package com.suman.newsfeed.infrastructure.database.jpa.entities;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "user_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNewsJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private NewsJpaEntity news;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    public UserNewsJpaEntity(Long id, UserJpaEntity user, NewsJpaEntity news, Boolean isRead) {
        this.id = id;
        this.user = user;
        this.news = news;
        this.isRead = isRead;
    }
}
