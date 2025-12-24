package com.suman.newsfeed.infrastructure.database.jpa.entities;


import com.suman.newsfeed.domain.user.UserKeyword;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserKeywordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "matched_count", nullable = false)
    private Integer matchedCount = 0;  // 키워드 매칭 횟수

    @Column(name = "last_matched_at")
    private LocalDateTime lastMatchedAt;  // 마지막 뉴스 매칭 시간

    // 기본 생성자 (필수 필드만)
    public UserKeywordJpaEntity(String text, UserJpaEntity user, LocalDateTime subscribedAt) {
        this.text = text;
        this.subscribedAt = subscribedAt;
        this.user = user;
        this.lastMatchedAt = null;  // 초기값은 null
        this.isActive = true;  // 기본값은 활성화 상태
        this.matchedCount = 0;  // 초기 매칭 횟수는 0
    }

    public void update(UserKeyword userKeyword) {
        this.text = userKeyword.getText();
        this.isActive = userKeyword.getIsActive();
        this.matchedCount = userKeyword.getMatchedCount();
        this.lastMatchedAt = userKeyword.getLastMatchedAt();
    }
}
