package com.suman.newsfeed.domain.user;


import com.suman.newsfeed.domain.news.News;
import lombok.Data;

@Data
public class UserNews {

    private Long id;
    private User user;
    private News news;
    private Boolean isRead;

    private UserNews(Long id, User user, News news, Boolean isRead) {
        this.id = id;
        this.user = user;
        this.news = news;
        this.isRead = isRead;
    }

    public static UserNews create(User user, News news) {
        //생성되었다는건 뉴스 조회 이력에 추가되었다는 의미
        return new UserNews(null, user, news,true);
    }

}
