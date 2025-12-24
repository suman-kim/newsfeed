package com.suman.newsfeed.infrastructure.database.jpa.entities;


import com.suman.newsfeed.domain.news.NewsKeyword;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "news_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsKeywordJpaEntity extends BaseEntity {
    private String text;
    //수집된 횟수
    @Column(name = "collected_count")
    private Long collectedCount;

    @OneToMany(mappedBy = "newsKeyword", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NewsJpaEntity> news = new HashSet<>();


    public NewsKeywordJpaEntity(Long id, String domainId, String text, Long collectedCount) {
        this.id = id;
        this.domainId = domainId;
        this.text = text;
        this.collectedCount = collectedCount;
    }

    public void update(NewsKeyword newsKeyword){
        System.out.println("newsKeyword 카운트 수 " + newsKeyword.getCollectedCount());
        this.collectedCount = newsKeyword.getCollectedCount();
    }
}
