package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserKeywordsUseCase {

    private final UserKeywordRepository userKeywordRepository;

    public List<UserKeyword> execute(Long userId) {
        return userKeywordRepository.findByUserId(userId);
    }

}
