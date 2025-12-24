package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserJpaRepository;
import com.suman.newsfeed.infrastructure.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public void save(User user) {
        UserJpaEntity userJpaEntity = userMapper.toEntity(user);
        userJpaRepository.save(userJpaEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUserIdWithKeywordsAndPlatforms(Long userId) {
        return userJpaRepository.findByUserIdWithKeywordsAndPlatforms(userId) // join fetch
                .map(userMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("사용자 중 해당하는 ID가 없습니다.: " + userId));
    }


    @Override
    @Transactional(readOnly = true)
    public User findByDomainId(String domainId) {
        return userMapper.toDomain(userJpaRepository.findByDomainId(domainId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 중 해당하는 도메인 ID가 없습니다: " + domainId)));
    }

    @Override
    public User findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("사용자 중 해당하는 이메일이 없습니다: " + email));
    }

    @Override
    public UserJpaEntity findById(Long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 중 해당하는 ID가 없습니다: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllWithKeywordsAndPlatforms(){
        return userMapper.toDomainList(userJpaRepository.findAllWithKeywordsAndPlatforms());
    }

}
