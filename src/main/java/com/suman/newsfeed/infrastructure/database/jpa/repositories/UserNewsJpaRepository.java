package com.suman.newsfeed.infrastructure.database.jpa.repositories;

import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNewsJpaRepository  extends JpaRepository<UserNewsJpaEntity,Long> {
}
