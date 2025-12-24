package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.domain.user.UserNewsPlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


// 사용자가 구독한 뉴스 플랫폼을 조회하는 UseCase
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserNewsPlatformsUseCase {

    private final UserNewsPlatformRepository userNewsPlatformRepository;

    /**
     * 사용자가 구독한 뉴스 플랫폼 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 구독 중인 뉴스 플랫폼 목록
     */
    public List<UserNewsPlatform> execute(Long userId) {
        return userNewsPlatformRepository.findAllByUserId(userId);
    }

}
