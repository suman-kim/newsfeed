package com.suman.newsfeed.infrastructure.mappers;


import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.domain.userNewsFeed.UserNewsFeed;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final UserKeywordMapper userKeywordMapper;
    private final UserNewsPlatformMapper userNewsPlatformMapper;

    public User toDomain(UserJpaEntity userJpaEntity) {

        Set<UserKeyword> userKeywords = userJpaEntity.getUserKeywords().stream()
                .map(userKeywordMapper::toDomain)
                .collect(Collectors.toSet());

        Set<UserNewsPlatform> userNewsPlatforms = userJpaEntity.getUserNewsPlatforms().stream()
                .map(userNewsPlatformMapper::toDomain)
                .collect(Collectors.toSet());


        return User.reconstruct(
                userJpaEntity.getId(),
                userJpaEntity.getDomainId(),
                userJpaEntity.getEmail(),
                userJpaEntity.getPassword(),
                userJpaEntity.getNickname(),
                userKeywords,
                userNewsPlatforms,
                userJpaEntity.getRefreshToken(),
                userJpaEntity.getRefreshTokenExpiresAt()
        );
    }

    public List<User> toDomainList(List<UserJpaEntity> userJpaEntities) {
        return userJpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getDomainId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getRole(),
                user.getRefreshToken(),
                user.getRefreshTokenExpiresAt()

        );
    }


}
