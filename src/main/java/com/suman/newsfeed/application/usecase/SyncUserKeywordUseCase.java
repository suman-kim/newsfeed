package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserKeywordRepository;
import com.suman.newsfeed.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SyncUserKeywordUseCase {

    private final UserKeywordRepository userKeywordRepository;
    private final UserRepository userRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final RemoveNewsKeywordUseCase removeNewsKeywordUseCase;

    /**
     * 키워드 동기화 (추가/삭제)
     * @param userId user ID
     * @param keywords 키워드 리스트
     */
    @Transactional
    public void execute(Long userId, List<String> keywords) {

        if (keywords == null) {
            keywords = Collections.emptyList();
        }

        log.info("키워드 동기화 시작 {} for user {}", keywords, userId);

        User user = userRepository.findByUserIdWithKeywordsAndPlatforms(userId);

        // 현재 사용자의 키워드 정보 (텍스트 -> UserKeyword 매핑)
        Map<String, UserKeyword> currentKeywordMap = user.getUserKeywords().stream()
                .collect(Collectors.toMap(
                        UserKeyword::getText,
                        userKeyword -> userKeyword,
                        (existing, replacement) -> existing // 중복 키 처리
                ));

        Set<String> currentKeywords = currentKeywordMap.keySet();

        // 새로운 키워드 Set (중복 제거 및 null/빈값 필터링)
        Set<String> newKeywordSet = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Set 연산을 활용한 효율적인 차집합 계산
        Set<String> keywordsToAdd = new HashSet<>(newKeywordSet);
        keywordsToAdd.removeAll(currentKeywords);

        Set<String> keywordsToRemove = new HashSet<>(currentKeywords);
        keywordsToRemove.removeAll(newKeywordSet);

        // 변경사항이 있는 경우만 처리
        if (keywordsToAdd.isEmpty() && keywordsToRemove.isEmpty()) {
            log.info("키워드 변경사항 없음 for user {}", userId);
            return;
        }

        // 키워드 삭제 처리
        if (!keywordsToRemove.isEmpty()) {
            log.info("삭제할 키워드: {}", keywordsToRemove);

            // 삭제할 UserKeyword ID
            List<Long> keywordIdsToDelete = keywordsToRemove.stream()
                    .map(currentKeywordMap::get)
                    .filter(Objects::nonNull)
                    .map(UserKeyword::getId)
                    .collect(Collectors.toList());

            // User 엔티티에서 키워드 제거
            user.removeKeywords(new ArrayList<>(keywordsToRemove));

            // DB에서 키워드 삭제
            userKeywordRepository.deleteAllByIds(keywordIdsToDelete);

        }

        // 키워드 추가 처리
        if (!keywordsToAdd.isEmpty()) {
            log.info("추가할 키워드: {}", keywordsToAdd);
            List<UserKeyword> newUserKeywords = keywordsToAdd.stream()
                    .map(user::addKeyword)
                    .collect(Collectors.toList());

            userKeywordRepository.saveAll(userId, newUserKeywords);
        }

        // 도메인 이벤트 발행
        domainEventPublisher.publishEvents(user);
        log.info("키워드 동기화 완료. 추가: {}, 삭제: {}", keywordsToAdd.size(), keywordsToRemove.size());
    }
}
