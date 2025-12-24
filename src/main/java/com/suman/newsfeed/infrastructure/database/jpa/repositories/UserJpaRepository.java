package com.suman.newsfeed.infrastructure.database.jpa.repositories;

import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Boolean existsByEmail(String email);
    Optional<UserJpaEntity> findByDomainId(String domainId);
    Optional<UserJpaEntity> findByEmail(String email);
    @Query("SELECT u FROM UserJpaEntity u " +
            "LEFT JOIN FETCH u.userKeywords " +
            "LEFT JOIN FETCH u.userNewsPlatforms " +
            "WHERE u.id = :userId")
    Optional<UserJpaEntity> findByUserIdWithKeywordsAndPlatforms(@Param("userId") Long userId);

    // userKeywords와 userNewsPlatform 모두 가져오는 메서드
    @Query("SELECT DISTINCT u FROM UserJpaEntity u " +
            "LEFT JOIN FETCH u.userKeywords " +
            "LEFT JOIN FETCH u.userNewsPlatforms ")
    List<UserJpaEntity> findAllWithKeywordsAndPlatforms();

}
