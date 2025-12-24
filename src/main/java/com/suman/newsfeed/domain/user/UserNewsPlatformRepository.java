package com.suman.newsfeed.domain.user;

import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsPlatformEntity;

import java.util.List;
import java.util.Optional;

public interface UserNewsPlatformRepository {

    UserNewsPlatform save(UserNewsPlatform userNewsPlatform);
    void saveAll(Long userId, List<UserNewsPlatform> userNewsPlatforms);
    List<UserNewsPlatform> findAllByUserId(Long userId);
    void deleteAllByIds(List<Long> ids);
}
