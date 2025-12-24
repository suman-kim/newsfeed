package com.suman.newsfeed.application.usecase;


import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.*;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import com.suman.newsfeed.presentation.dto.request.SubscribeKeywordRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddUserNewsPlatformUseCase {

    private final UserNewsPlatformRepository userNewsPlatformRepository;

    /**
     * 뉴스 플랫폼 동기화 (추가/삭제)
     * @param userId user 도메인 ID
     * @param platformNameList 최종 플랫폼 리스트
     */
    public void execute(Long userId, List<String> platformNameList) {
        log.info("사용자 {}에 대한 뉴스 플랫폼 동기화 시작: {}", userId, platformNameList);

        Set<NewsPlatform> currentPlatforms = userNewsPlatformRepository
                .findAllByUserId(userId)
                .stream()
                .map(UserNewsPlatform::getNewsPlatform)
                .collect(Collectors.toSet());

        // 유효한 플랫폼만 필터링
        List<NewsPlatform> validNewPlatforms = platformNameList.stream()
                .map(NewsPlatform::fromString)
                .filter(Objects::nonNull)
                .toList();

        Set<NewsPlatform> newPlatformSet = new HashSet<>(validNewPlatforms);

        // 추가할 플랫폼
        List<NewsPlatform> platformsToAdd = validNewPlatforms.stream()
                .filter(platform -> !currentPlatforms.contains(platform))
                .toList();

        // 삭제할 플랫폼
        List<Long> platformsToRemove = userNewsPlatformRepository.findAllByUserId(userId).stream()
                .filter(userNewsPlatform -> !newPlatformSet.contains(userNewsPlatform.getNewsPlatform()))
                .map(UserNewsPlatform::getId)
                .toList();

        // 삭제 처리
        if (!platformsToRemove.isEmpty()) {
            log.info("사용자 {}의 삭제할 플랫폼: {}", userId, platformsToRemove);
            userNewsPlatformRepository.deleteAllByIds(platformsToRemove);
        }

        // 추가 처리
        if (!platformsToAdd.isEmpty()) {
            log.info("사용자 {}의 추가할 플랫폼: {}", userId, platformsToAdd);
            List<UserNewsPlatform> newUserPlatforms = platformsToAdd.stream()
                    .map(platform -> UserNewsPlatform.create(platform, userId))
                    .toList();
            userNewsPlatformRepository.saveAll(userId, newUserPlatforms);
        }

        if (!platformsToAdd.isEmpty() || !platformsToRemove.isEmpty()) {
            log.info("사용자 {}의 플랫폼 동기화 완료. 추가: {}, 삭제: {}",
                    userId, platformsToAdd.size(), platformsToRemove.size());
        }
        else {
            log.info("사용자 {}의 플랫폼 변경사항 없음", userId);
        }
    }
}
