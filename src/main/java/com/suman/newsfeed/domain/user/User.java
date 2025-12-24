package com.suman.newsfeed.domain.user;


import com.suman.newsfeed.domain.shared.BaseAggregate;
import com.suman.newsfeed.domain.user.event.UserKeywordActiveEvent;
import com.suman.newsfeed.domain.user.event.UserKeywordAddedEvent;
import com.suman.newsfeed.domain.user.event.UserKeywordRemovedEvent;
import com.suman.newsfeed.domain.user.event.UserRegisteredEvent;
import com.suman.newsfeed.domain.userNewsFeed.UserNewsFeed;
import com.suman.newsfeed.shared.UserRole;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class User extends BaseAggregate {

    private Long id; // DB에서 사용될 ID
    private String email;
    private String password;
    private String nickname;
    private UserRole role;
    private Set<UserKeyword> userKeywords; // 사용자 키워드
    private Set<UserNewsPlatform> userNewsPlatforms; // 사용자 뉴스 플랫폼
    private String refreshToken; // Refresh Token
    private LocalDateTime refreshTokenExpiresAt; // Refresh Token 만료 시간

    // 생성자
    private User(Long id,
                 String domainId,
                 String email,
                 String password,
                 String nickname,
                 Set<UserKeyword> userKeywords,
                 Set <UserNewsPlatform> userNewsPlatforms,
                 String refreshToken,
                 LocalDateTime refreshTokenExpiresAt) {
        super(domainId);

        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = UserRole.USER;
        this.userKeywords = userKeywords != null ? userKeywords : new HashSet<>();
        this.userNewsPlatforms = userNewsPlatforms != null ? userNewsPlatforms : new HashSet<>();
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public static User create(String email, String password, String nickname) {
        String domainId = UUID.randomUUID().toString();
        User user = new User(null, domainId, email, password, nickname, null ,null,null,null);

        //도메인 이벤트 생성
        user.addDomainEvent(new UserRegisteredEvent(
                domainId,
                user.email,
                user.nickname
        ));

        return user;
    }

    //매퍼에서 사용
    public static User reconstruct(Long id,
                                   String domainId,
                                   String email,
                                   String password,
                                   String nickname,
                                   Set<UserKeyword> userKeywords,
                                   Set<UserNewsPlatform> userNewsPlatforms,
                                   String refreshToken,
                                   LocalDateTime refreshTokenExpiresAt) {
        return new User(id, domainId, email, password, nickname, userKeywords, userNewsPlatforms, refreshToken, refreshTokenExpiresAt);
    }


    // 키워드 생성
    public UserKeyword addKeyword(String text) {

        if (hasKeyword(text)) {
            throw new IllegalArgumentException("이미 등록된 키워드입니다: " + text);
        }

        if (userKeywords.size() >= 50) {
            throw new IllegalArgumentException("키워드는 최대 50개까지 등록 가능합니다.");
        }

        UserKeyword userKeyword = UserKeyword.create(text,this.getId());
        this.userKeywords.add(userKeyword);
        addDomainEvent(new UserKeywordAddedEvent(this.domainId, text));
        return userKeyword;
    }

    //키워드 삭제
    public void removeKeywords(List<String> keywordTexts) {
        for (String keywordText : keywordTexts) {
            UserKeyword userKeyword = userKeywords.stream()
                    .filter(uk -> uk.getText().equals(keywordText))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("해당 키워드를 찾을 수 없습니다: " + keywordText));

            this.userKeywords.remove(userKeyword);
        }

        //TODO: 도메인 이벤트 발행
        addDomainEvent(new UserKeywordRemovedEvent(this.domainId,keywordTexts));
    }

    // 중복 키워드 확인
    private boolean hasKeyword(String keywordText) {
        return userKeywords.stream()
                .anyMatch(uk -> uk.getText().equals(keywordText));
    }


    //키워드 조회
    public UserKeyword getKeywordById(Long keywordId) {
        return userKeywords.stream()
                .filter(uk -> uk.getId().equals(keywordId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 키워드를 찾을 수 없습니다: " + keywordId));
    }


    public void activeKeyword(Long keywordId) {
        UserKeyword userKeyword = getKeywordById(keywordId);
        userKeyword.active();
        // 도메인 이벤트 생성
        addDomainEvent(new UserKeywordActiveEvent(this.domainId, userKeyword.getText()));
    }

    // Refresh Token 만료 확인
    public boolean isRefreshTokenExpired() {
        return refreshTokenExpiresAt == null || refreshTokenExpiresAt.isBefore(LocalDateTime.now());
    }

    // Refresh Token 업데이트 메서드
    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    // Refresh Token 무효화 (로그아웃)
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }
}
