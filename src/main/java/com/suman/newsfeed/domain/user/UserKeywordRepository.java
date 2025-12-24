package com.suman.newsfeed.domain.user;

import java.util.List;

public interface UserKeywordRepository {
    void save(UserKeyword userKeyword);
    void saveAll(Long userId, List<UserKeyword> userKeywords);
    UserKeyword findById(Long id);
    void update(UserKeyword userKeyword);
    List<UserKeyword> findAllByIsActiveTrue();
    void deleteAllByIds (List<Long> ids);
    List<UserKeyword> findByUserId(Long userId);
}
