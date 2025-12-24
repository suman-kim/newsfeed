package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.TestDataBuilder;
import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserKeywordRepository;
import com.suman.newsfeed.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * AddUserKeywordUseCase 테스트 클래스
 * 사용자 키워드 추가/삭제 비즈니스 로직에 대한 종합적인 테스트를 수행합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddUserKeywordUseCase 테스트")
class SyncUserKeywordUseCaseTest {

    @Mock
    private UserKeywordRepository userKeywordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private SyncUserKeywordUseCase syncUserKeywordUseCase;

    private User testUser;
    private List<String> newKeywords;
    private List<String> existingKeywords;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = TestDataBuilder.createTestUserWithId();
        
        // 기존 키워드 설정
        UserKeyword existingKeyword1 = TestDataBuilder.createTestUserKeywordWithId();
        existingKeyword1.setId(1L);
        existingKeyword1.setText("기존키워드1");
        
        UserKeyword existingKeyword2 = TestDataBuilder.createTestUserKeywordWithId();
        existingKeyword2.setId(2L);
        existingKeyword2.setText("기존키워드2");
        
        testUser.getUserKeywords().add(existingKeyword1);
        testUser.getUserKeywords().add(existingKeyword2);
        
        // 새로운 키워드 리스트
        newKeywords = Arrays.asList("기존키워드1", "기존키워드2", "새로운키워드1", "새로운키워드2");
        existingKeywords = Arrays.asList("기존키워드1", "기존키워드2");
    }

    @Test
    @DisplayName("새로운 키워드 추가 테스트")
    void shouldAddNewKeywordsSuccessfully() {
        // Given
        List<String> keywordsToAdd = Arrays.asList("새로운키워드1", "새로운키워드2");
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);
        doNothing().when(userKeywordRepository).saveAll(anyLong(), anyList());

        // When
        syncUserKeywordUseCase.execute(1L, newKeywords);

        // Then
        verify(userKeywordRepository, times(1)).saveAll(eq(1L), anyList());
        verify(domainEventPublisher, times(1)).publishEvents(testUser);
        
        // 사용자에게 새로운 키워드가 추가되었는지 확인
        assertTrue(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("새로운키워드1")));
        assertTrue(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("새로운키워드2")));
    }

    @Test
    @DisplayName("기존 키워드 유지 테스트")
    void shouldKeepExistingKeywords() {
        // Given
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);

        // When
        syncUserKeywordUseCase.execute(1L, existingKeywords);

        // Then
        verify(userKeywordRepository, never()).saveAll(anyLong(), anyList());
        verify(userKeywordRepository, never()).deleteAllByIds(anyList());
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
        
        // 기존 키워드가 그대로 유지되는지 확인
        assertEquals(2, testUser.getUserKeywords().size());
        assertTrue(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("기존키워드1")));
        assertTrue(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("기존키워드2")));
    }

    @Test
    @DisplayName("키워드 삭제 테스트")
    void shouldRemoveKeywordsSuccessfully() {
        // Given
        List<String> reducedKeywords = Arrays.asList("기존키워드1"); // 기존키워드2 제거
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);
        doNothing().when(userKeywordRepository).deleteAllByIds(anyList());

        // When
        syncUserKeywordUseCase.execute(1L, reducedKeywords);

        // Then
        verify(userKeywordRepository, times(1)).deleteAllByIds(anyList());
        verify(domainEventPublisher, times(1)).publishEvents(testUser);
        
        // 사용자에게서 키워드가 제거되었는지 확인
        assertTrue(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("기존키워드1")));
        assertFalse(testUser.getUserKeywords().stream()
                .anyMatch(keyword -> keyword.getText().equals("기존키워드2")));
    }

    @Test
    @DisplayName("키워드 추가 및 삭제 동시 처리 테스트")
    void shouldHandleAddAndRemoveSimultaneously() {
        // Given
        List<String> mixedKeywords = Arrays.asList("기존키워드1", "새로운키워드1", "새로운키워드2");
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);
        doNothing().when(userKeywordRepository).saveAll(anyLong(), anyList());
        doNothing().when(userKeywordRepository).deleteAllByIds(anyList());

        // When
        syncUserKeywordUseCase.execute(1L, mixedKeywords);

        // Then
        verify(userKeywordRepository, times(1)).saveAll(eq(1L), anyList());
        verify(userKeywordRepository, times(1)).deleteAllByIds(anyList());
        verify(domainEventPublisher, times(1)).publishEvents(testUser);
    }

    @Test
    @DisplayName("변경사항이 없을 때 도메인 이벤트 미발행 테스트")
    void shouldNotPublishEventsWhenNoChanges() {
        // Given
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);

        // When
        syncUserKeywordUseCase.execute(1L, existingKeywords);

        // Then
        verify(userKeywordRepository, never()).saveAll(anyLong(), anyList());
        verify(userKeywordRepository, never()).deleteAllByIds(anyList());
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
    }

    @Test
    @DisplayName("사용자 조회 실패 시 예외 처리 테스트")
    void shouldHandleUserNotFoundGracefully() {
        // Given
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, () -> syncUserKeywordUseCase.execute(1L, newKeywords));
        
        verify(userKeywordRepository, never()).saveAll(anyLong(), anyList());
        verify(userKeywordRepository, never()).deleteAllByIds(anyList());
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
    }

    @Test
    @DisplayName("빈 키워드 리스트 처리 테스트")
    void shouldHandleEmptyKeywordList() {
        // Given
        List<String> emptyKeywords = Arrays.asList();
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);

        // When
        syncUserKeywordUseCase.execute(1L, emptyKeywords);

        // Then
        verify(userKeywordRepository, never()).saveAll(anyLong(), anyList());
        verify(userKeywordRepository, times(1)).deleteAllByIds(anyList());
        verify(domainEventPublisher, times(1)).publishEvents(testUser);
        
        // 모든 키워드가 제거되었는지 확인
        assertTrue(testUser.getUserKeywords().isEmpty());
    }

    @Test
    @DisplayName("키워드 저장 실패 시 예외 처리 테스트")
    void shouldHandleSaveFailureGracefully() {
        // Given
        List<String> keywordsToAdd = Arrays.asList("새로운키워드1");
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(anyLong())).thenReturn(testUser);
        doThrow(new RuntimeException("Database error")).when(userKeywordRepository).saveAll(anyLong(), anyList());

        // When & Then
        assertThrows(RuntimeException.class, () -> syncUserKeywordUseCase.execute(1L, keywordsToAdd));
        
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
    }
}
