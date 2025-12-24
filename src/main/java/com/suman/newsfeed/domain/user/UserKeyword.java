package com.suman.newsfeed.domain.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserKeyword {
    private Long id; // DB에서 사용될 ID
    private String text;
    private Long userId;
    private Boolean isActive;
    private Integer matchedCount;
    private LocalDateTime subscribedAt;
    private LocalDateTime lastMatchedAt;

    private UserKeyword(Long id, String text, Long userId, Boolean isActive,Integer matchedCount, LocalDateTime subscribedAt, LocalDateTime lastMatchedAt) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.isActive = isActive;
        this.matchedCount = matchedCount != null ? matchedCount : 0; // 초기 매칭 횟수는 0
        this.subscribedAt = subscribedAt != null ? subscribedAt : LocalDateTime.now();
        this.lastMatchedAt = lastMatchedAt;
    }

    public static UserKeyword create(String text, Long userId) {
        return new UserKeyword(null, text, userId, true, 0, LocalDateTime.now(), null);
    }

    public static UserKeyword reconstruct(Long id, String text, Long userId, Boolean isActive, Integer matchedCount, LocalDateTime subscribedAt, LocalDateTime lastMatchedAt) {
        return new UserKeyword(id, text, userId, isActive, matchedCount, subscribedAt, lastMatchedAt);
    }


    public void incrementMatchedCount() {
        System.out.println("Incrementing matched count for keyword: " + this.matchedCount);
        this.matchedCount++;
        this.lastMatchedAt = LocalDateTime.now();
    }




    public void active() {
        this.isActive = this.isActive != true;
    }
}
