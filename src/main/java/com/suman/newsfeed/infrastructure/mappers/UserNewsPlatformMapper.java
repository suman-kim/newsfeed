package com.suman.newsfeed.infrastructure.mappers;

import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsPlatformEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserNewsPlatformMapper {



     public UserNewsPlatformEntity toEntity(UserNewsPlatform userNewsPlatform, UserJpaEntity user) {
         return new UserNewsPlatformEntity(
             userNewsPlatform.getId(),
             userNewsPlatform.getNewsPlatform(),
             user
         );
     }

    public UserNewsPlatform toDomain(UserNewsPlatformEntity userNewsPlatformEntity) {
        return UserNewsPlatform.reconstruct(
                userNewsPlatformEntity.getId(),
                userNewsPlatformEntity.getNewsPlatform(),
                userNewsPlatformEntity.getUser().getId()
        );
    }

    public List<UserNewsPlatform> toDomainList(List<UserNewsPlatformEntity> userNewsPlatformEntities) {
        return userNewsPlatformEntities.stream()
                .map(this::toDomain)
                .toList();
    }

    public List<UserNewsPlatformEntity> toEntityList(List<UserNewsPlatform> userNewsPlatforms, UserJpaEntity user) {
        return userNewsPlatforms.stream()
                .map(userNewsPlatform -> toEntity(userNewsPlatform, user))
                .toList();
    }
}
