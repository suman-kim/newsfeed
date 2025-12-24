package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserKeywordRepository;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserKeywordJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserKeywordJpaRepository;
import com.suman.newsfeed.infrastructure.mappers.UserKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserKeywordRepositoryAdapter implements UserKeywordRepository {

    private final UserKeywordJpaRepository userKeywordJpaRepository;
    private final UserRepository userRepository;
    private final UserKeywordMapper userKeywordMapper;

    @Override
    public void save(UserKeyword userKeyword) {
        UserJpaEntity userJpaEntity = userRepository.findById(userKeyword.getId());
        UserKeywordJpaEntity entity = userKeywordMapper.toEntity(userKeyword, userJpaEntity);
        userKeywordJpaRepository.save(entity);
    }

    @Override
    public void saveAll(Long userId,List<UserKeyword> userKeywords) {
        UserJpaEntity userJpaEntity = userRepository.findById(userId);
        List<UserKeywordJpaEntity> entities = userKeywordMapper.toEntityList(userKeywords, userJpaEntity);
        userKeywordJpaRepository.saveAll(entities);
    }

    @Override
    public UserKeyword findById(Long id) {
        return userKeywordJpaRepository.findById(id)
                .map(userKeywordMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("UserKeyword not found with id: " + id));
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        userKeywordJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void update(UserKeyword userKeyword) {
        UserKeywordJpaEntity entity = userKeywordJpaRepository.findById(userKeyword.getId())
                .orElseThrow(() -> new IllegalArgumentException("UserKeyword not found"));

        entity.update(userKeyword);

    }

    @Override
    public List<UserKeyword> findAllByIsActiveTrue() {
        return userKeywordMapper.toDomainList(userKeywordJpaRepository.findAllByIsActiveTrue());
    }

    @Override
    public List<UserKeyword> findByUserId(Long userId) {
        return userKeywordMapper.toDomainList(userKeywordJpaRepository.findByUserId(userId));
    }
}
