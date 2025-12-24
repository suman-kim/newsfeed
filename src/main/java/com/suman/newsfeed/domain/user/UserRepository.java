package com.suman.newsfeed.domain.user;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;

import java.util.List;

public interface UserRepository {
    Boolean existsByEmail(String email);
    User findByDomainId(String domainId);
    User findByUserIdWithKeywordsAndPlatforms(Long userId);
    User findByEmail(String email);
    void save(User user);
    UserJpaEntity findById(Long userId);
    List<User> findAllWithKeywordsAndPlatforms();

}
