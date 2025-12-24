package com.suman.newsfeed.infrastructure.mappers;


import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserKeywordJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserKeywordMapper {

    public UserKeyword toDomain(UserKeywordJpaEntity userKeywordJpaEntity) {

        return UserKeyword.reconstruct(
                userKeywordJpaEntity.getId(),
                userKeywordJpaEntity.getText(),
                userKeywordJpaEntity.getUser().getId(),
                userKeywordJpaEntity.getIsActive(),
                userKeywordJpaEntity.getMatchedCount(),
                userKeywordJpaEntity.getSubscribedAt(),
                userKeywordJpaEntity.getLastMatchedAt()
        );
    }

    public UserKeywordJpaEntity toEntity(UserKeyword userKeyword, UserJpaEntity userJpaEntity) {
        return new UserKeywordJpaEntity(
                userKeyword.getText(),
                userJpaEntity,
                userKeyword.getSubscribedAt()
        );
    }


    public List<UserKeyword> toDomainList(List<UserKeywordJpaEntity> userKeywordJpaEntities) {
        return userKeywordJpaEntities.stream()
                .map(this::toDomain)
                .toList();
    }

    public List<UserKeywordJpaEntity> toEntityList(List<UserKeyword> userKeywords,UserJpaEntity userJpaEntity) {
        return userKeywords.stream()
                .map(userKeyword -> toEntity(userKeyword, userJpaEntity))
                .toList();
    }
}
