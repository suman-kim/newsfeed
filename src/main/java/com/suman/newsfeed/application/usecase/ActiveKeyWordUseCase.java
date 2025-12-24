package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserKeywordRepository;
import com.suman.newsfeed.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActiveKeyWordUseCase {

    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 키워드 활성화 및 해제
     * @param userId user 도메인 ID
     * @param keywordId 활성화 & 해제할 키워드 ID
     */
    public void execute(Long userId, Long keywordId) {
        try {
            log.info("키워드 구독 취소 시작 {} from domain {}", keywordId, userId);

            User user = userRepository.findByUserIdWithKeywordsAndPlatforms(userId);
            UserKeyword userKeyword = userKeywordRepository.findById(keywordId);
            //키워드 활성화 또는 비활성

            //가지고 있는 키워드 확인
            log.info("사용자의 키워드 갯수 -> {} ", user.getUserKeywords().size());

            user.activeKeyword(keywordId);

            log.info("키워드 활성화/비활성화 완료: {}", userKeyword);
            //키워드 업데이트
            userKeywordRepository.update(userKeyword);

            //도메인 이벤트 발행
            domainEventPublisher.publishEvents(user);
        }
        catch (Exception e) {
            log.error("키워드 활성화/비활성화 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("키워드 활성화/비활성화 실패: " + e.getMessage(), e);
        }
    }
}
