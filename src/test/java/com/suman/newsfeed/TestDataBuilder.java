package com.suman.newsfeed;

import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserNews;
import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.domain.userNewsFeed.UserNewsFeed;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 테스트 데이터 생성을 위한 헬퍼 클래스
 * 각 도메인 객체의 테스트 인스턴스를 쉽게 생성할 수 있도록 도와줍니다.
 */
public class TestDataBuilder {

    /**
     * 테스트용 User 객체 생성
     */
    public static User createTestUser() {
        return User.create("test@example.com", "password123", "테스트유저");
    }

    /**
     * 테스트용 User 객체 생성 (ID 포함)
     */
    public static User createTestUserWithId() {
        User user = createTestUser();
        // ID 설정을 위해 reflection 사용 (실제로는 매퍼를 통해 설정됨)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return user;
    }

    /**
     * 테스트용 NewsKeyword 객체 생성
     */
    public static NewsKeyword createTestNewsKeyword() {
        return NewsKeyword.create("테스트키워드");
    }

    /**
     * 테스트용 NewsKeyword 객체 생성 (ID 포함)
     */
    public static NewsKeyword createTestNewsKeywordWithId() {
        NewsKeyword keyword = createTestNewsKeyword();
        try {
            java.lang.reflect.Field idField = NewsKeyword.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(keyword, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return keyword;
    }

    /**
     * 테스트용 News 객체 생성
     */
    public static News createTestNews() {
        return News.create(
                "테스트 뉴스 제목",
                "테스트 뉴스 내용입니다.",
                "https://example.com/news/1",
                createTestNewsKeyword(),
                NewsPlatform.NAVER,
                "https://example.com/image.jpg"
        );
    }

    /**
     * 테스트용 News 객체 생성 (ID 포함)
     */
    public static News createTestNewsWithId() {
        News news = createTestNews();
        try {
            java.lang.reflect.Field idField = News.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(news, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return news;
    }

    /**
     * 테스트용 UserKeyword 객체 생성
     */
    public static UserKeyword createTestUserKeyword() {
        return UserKeyword.create("사용자키워드", 1L);
    }

    /**
     * 테스트용 UserKeyword 객체 생성 (ID 포함)
     */
    public static UserKeyword createTestUserKeywordWithId() {
        UserKeyword userKeyword = createTestUserKeyword();
        try {
            java.lang.reflect.Field idField = UserKeyword.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userKeyword, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return userKeyword;
    }

    /**
     * 테스트용 UserNewsPlatform 객체 생성
     */
    public static UserNewsPlatform createTestUserNewsPlatform() {
        return UserNewsPlatform.create(NewsPlatform.NAVER, 1L);
    }

    /**
     * 테스트용 UserNewsPlatform 객체 생성 (ID 포함)
     */
    public static UserNewsPlatform createTestUserNewsPlatformWithId() {
        UserNewsPlatform platform = createTestUserNewsPlatform();
        try {
            java.lang.reflect.Field idField = UserNewsPlatform.class.getDeclaredField("Id");
            idField.setAccessible(true);
            idField.set(platform, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return platform;
    }

    /**
     * 테스트용 UserNews 객체 생성
     */
    public static UserNews createTestUserNews() {
        return UserNews.create(createTestUserWithId(), createTestNewsWithId());
    }

    /**
     * 테스트용 UserNews 객체 생성 (ID 포함)
     */
    public static UserNews createTestUserNewsWithId() {
        UserNews userNews = createTestUserNews();
        try {
            java.lang.reflect.Field idField = UserNews.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userNews, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return userNews;
    }

    /**
     * 테스트용 UserNewsFeed 객체 생성
     */
    public static UserNewsFeed createTestUserNewsFeed() {
        return UserNewsFeed.create(
                1L, // userId
                1L, // userKeywordId
                "테스트 뉴스 제목",
                "테스트 뉴스 내용입니다.",
                "https://example.com/news/1",
                NewsPlatform.NAVER,
                false // isRead
        );
    }

    /**
     * 테스트용 UserNewsFeed 객체 생성 (ID 포함)
     */
    public static UserNewsFeed createTestUserNewsFeedWithId() {
        UserNewsFeed feed = createTestUserNewsFeed();
        try {
            java.lang.reflect.Field idField = UserNewsFeed.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(feed, 1L);
        } catch (Exception e) {
            // 테스트 환경에서는 무시
        }
        return feed;
    }
}
