package com.suman.newsfeed.domain.user;


import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Data;

@Data
public class UserNewsPlatform {

    private Long Id;
    private NewsPlatform newsPlatform;
    private Long userId;


    private UserNewsPlatform(Long id, NewsPlatform newsPlatform, Long userId) {
        this.Id = id;
        this.newsPlatform = newsPlatform;
        this.userId = userId;
    }

    public static UserNewsPlatform create(NewsPlatform newsPlatform, Long userId) {
        return new UserNewsPlatform(null, newsPlatform, userId);
    }

    // 매퍼에서 사용
    public static UserNewsPlatform reconstruct(Long id, NewsPlatform newsPlatform, Long userId) {
        return new UserNewsPlatform(id, newsPlatform, userId);
    }
}
