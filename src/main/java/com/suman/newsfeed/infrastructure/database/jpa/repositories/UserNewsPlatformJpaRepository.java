package com.suman.newsfeed.infrastructure.database.jpa.repositories;

import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsPlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface UserNewsPlatformJpaRepository extends JpaRepository<UserNewsPlatformEntity, Long> {
    List<UserNewsPlatformEntity> findAllByUserId(Long userId);
}
