package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.domain.user.UserNewsPlatformRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsPlatformEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserJpaRepository;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserNewsPlatformJpaRepository;

import com.suman.newsfeed.infrastructure.mappers.UserNewsPlatformMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Repository
public class UserNewsPlatformRepositoryAdapter implements UserNewsPlatformRepository {
    private final UserNewsPlatformJpaRepository userNewsPlatformJpaRepository;
    private final UserNewsPlatformMapper userNewsPlatformMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public UserNewsPlatform save(UserNewsPlatform userNewsPlatform) {
        UserJpaEntity user = userJpaRepository.findById(userNewsPlatform.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자: " + userNewsPlatform.getUserId()));

        return userNewsPlatformMapper.toDomain(userNewsPlatformJpaRepository.save(userNewsPlatformMapper.toEntity(userNewsPlatform,user)));
    }

    @Override
    public void saveAll(Long userId, List<UserNewsPlatform> userNewsPlatforms) {
        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자: " + userId));

        List<UserNewsPlatformEntity> entities = userNewsPlatformMapper.toEntityList(userNewsPlatforms, user);
        userNewsPlatformJpaRepository.saveAll(entities);
    }

    @Override
    public List<UserNewsPlatform> findAllByUserId(Long userId) {
        List<UserNewsPlatformEntity> userNewsPlatformEntities = userNewsPlatformJpaRepository.findAllByUserId(userId);
        return userNewsPlatformMapper.toDomainList(userNewsPlatformEntities);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        userNewsPlatformJpaRepository.deleteAllByIdInBatch(ids);
    }

}
