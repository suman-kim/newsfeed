package com.suman.newsfeed.domain.userNewsFeed;

import com.suman.newsfeed.domain.shared.BaseAggregate;
import com.suman.newsfeed.domain.userNewsFeed.event.UserNewsFeedAddedEvent;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import lombok.Data;


@Data
public class UserNewsFeed extends BaseAggregate {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String url;
    private NewsPlatform newsPlatform;
    private Long userKeywordId;
    private Boolean isRead;


    private UserNewsFeed(Long id,String domainId, Long userId,String title,String content,String url, NewsPlatform newsPlatform,  Long userKeywordId, Boolean isRead){
        super(domainId);
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.url = url;
        this.newsPlatform = newsPlatform;
        this.userKeywordId = userKeywordId;
        this.isRead = isRead;
    }


    public static UserNewsFeed create(Long userId, Long userKeywordId,String title, String content, String url, NewsPlatform newsPlatform, Boolean isRead) {
        String domainId = java.util.UUID.randomUUID().toString();
        UserNewsFeed userNewsFeed = new UserNewsFeed(null, domainId, userId,title,content,url, newsPlatform,  userKeywordId, isRead);

        //TODO: 도메인 이벤트 추가
        userNewsFeed.addDomainEvent(new UserNewsFeedAddedEvent(
                domainId,
                userId.toString(),
                title,
                content,
                url,
                newsPlatform,
                userKeywordId,
                isRead
        ));

        return userNewsFeed;
    }

    public static UserNewsFeed reconstruct(Long id, String domainId, Long userId, String title, String content, String url, NewsPlatform newsPlatform, Long userKeywordId, Boolean isRead) {
        return new UserNewsFeed(id, domainId, userId, title, content, url, newsPlatform, userKeywordId, isRead);
    }
}
