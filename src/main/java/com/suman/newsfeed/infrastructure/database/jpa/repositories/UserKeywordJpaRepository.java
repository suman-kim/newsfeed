package com.suman.newsfeed.infrastructure.database.jpa.repositories;

import com.suman.newsfeed.infrastructure.database.jpa.entities.UserKeywordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserKeywordJpaRepository extends JpaRepository<UserKeywordJpaEntity, Long> {

    List<UserKeywordJpaEntity> findAllByIsActiveTrue();
    List<UserKeywordJpaEntity> findByUserId(Long userId);
}
