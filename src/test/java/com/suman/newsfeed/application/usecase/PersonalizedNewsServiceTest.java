package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.TestDataBuilder;
import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import com.suman.newsfeed.presentation.dto.response.PersonalizedNewsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * PersonalizedNewsService 테스트 클래스
 * 개인화된 뉴스 조회 비즈니스 로직에 대한 종합적인 테스트를 수행합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalizedNewsService 테스트")
class PersonalizedNewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsKeywordRepository newsKeywordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PersonalizedNewsService personalizedNewsService;

    private User testUser;
    private List<NewsKeyword> testNewsKeywords;
    private List<News> testNews;
    private List<PersonalizedNewsResponse> expectedResponses;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = TestDataBuilder.createTestUserWithId();
        
        // 사용자 키워드 설정
        UserKeyword userKeyword1 = TestDataBuilder.createTestUserKeywordWithId();
        userKeyword1.setText("테스트키워드1");
        UserKeyword userKeyword2 = TestDataBuilder.createTestUserKeywordWithId();
        userKeyword2.setText("테스트키워드2");
        testUser.getUserKeywords().add(userKeyword1);
        testUser.getUserKeywords().add(userKeyword2);
        
        // 사용자 뉴스 플랫폼 설정
        UserNewsPlatform platform1 = TestDataBuilder.createTestUserNewsPlatformWithId();
        platform1.setNewsPlatform(NewsPlatform.NAVER);
        UserNewsPlatform platform2 = TestDataBuilder.createTestUserNewsPlatformWithId();
        platform2.setNewsPlatform(NewsPlatform.GOOGLE);
        testUser.getUserNewsPlatforms().add(platform1);
        testUser.getUserNewsPlatforms().add(platform2);
        
        // 테스트용 뉴스 키워드
        NewsKeyword newsKeyword1 = TestDataBuilder.createTestNewsKeywordWithId();
        newsKeyword1.setText("테스트키워드1");
        NewsKeyword newsKeyword2 = TestDataBuilder.createTestNewsKeywordWithId();
        newsKeyword2.setText("테스트키워드2");
        testNewsKeywords = Arrays.asList(newsKeyword1, newsKeyword2);
        
        // 테스트용 뉴스
        News news1 = TestDataBuilder.createTestNewsWithId();
        news1.setNewsKeyword(newsKeyword1);
        news1.setPlatform(NewsPlatform.NAVER);
        News news2 = TestDataBuilder.createTestNewsWithId();
        news2.setNewsKeyword(newsKeyword2);
        news2.setPlatform(NewsPlatform.GOOGLE);
        testNews = Arrays.asList(news1, news2);
        
        // 예상 응답
        expectedResponses = Arrays.asList(
                PersonalizedNewsResponse.builder()
                        .newsId(news1.getId())
                        .title(news1.getTitle())
                        .content(news1.getContent())
                        .url(news1.getUrl())
                        .platform(news1.getPlatform())
                        .matchedKeyword(news1.getNewsKeyword().getText())
                        .build(),
                PersonalizedNewsResponse.builder()
                        .newsId(news2.getId())
                        .title(news2.getTitle())
                        .content(news2.getContent())
                        .url(news2.getUrl())
                        .platform(news2.getPlatform())
                        .matchedKeyword(news2.getNewsKeyword().getText())
                        .build()
        );
    }

    @Test
    @DisplayName("정상적인 개인화 뉴스 조회 테스트")
    void shouldGetPersonalizedNewsSuccessfully() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(testUser);
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenReturn(testNews);

        // When
        List<PersonalizedNewsResponse> result = personalizedNewsService.getPersonalizedNews(userId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // 첫 번째 뉴스 검증
        PersonalizedNewsResponse firstNews = result.get(0);
        assertEquals(testNews.get(0).getId(), firstNews.getNewsId());
        assertEquals(testNews.get(0).getTitle(), firstNews.getTitle());
        assertEquals(testNews.get(0).getContent(), firstNews.getContent());
        assertEquals(testNews.get(0).getUrl(), firstNews.getUrl());
        assertEquals(testNews.get(0).getPlatform(), firstNews.getPlatform());
        assertEquals(testNews.get(0).getNewsKeyword().getText(), firstNews.getMatchedKeyword());
        
        // 두 번째 뉴스 검증
        PersonalizedNewsResponse secondNews = result.get(1);
        assertEquals(testNews.get(1).getId(), secondNews.getNewsId());
        assertEquals(testNews.get(1).getTitle(), secondNews.getTitle());
        assertEquals(testNews.get(1).getContent(), secondNews.getContent());
        assertEquals(testNews.get(1).getUrl(), secondNews.getUrl());
        assertEquals(testNews.get(1).getPlatform(), secondNews.getPlatform());
        assertEquals(testNews.get(1).getNewsKeyword().getText(), secondNews.getMatchedKeyword());
        
        verify(userRepository, times(1)).findByUserIdWithKeywordsAndPlatforms(userId);
        verify(newsKeywordRepository, times(1)).findAll();
        verify(newsRepository, times(1)).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 키워드가 없을 때 빈 결과 반환 테스트")
    void shouldReturnEmptyResultWhenUserHasNoKeywords() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        User userWithoutKeywords = TestDataBuilder.createTestUserWithId();
        userWithoutKeywords.getUserKeywords().clear();
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(userWithoutKeywords);
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenReturn(Arrays.asList());

        // When
        List<PersonalizedNewsResponse> result = personalizedNewsService.getPersonalizedNews(userId, page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(newsRepository, times(1)).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 플랫폼이 없을 때 빈 결과 반환 테스트")
    void shouldReturnEmptyResultWhenUserHasNoPlatforms() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        User userWithoutPlatforms = TestDataBuilder.createTestUserWithId();
        userWithoutPlatforms.getUserNewsPlatforms().clear();
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(userWithoutPlatforms);
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenReturn(Arrays.asList());

        // When
        List<PersonalizedNewsResponse> result = personalizedNewsService.getPersonalizedNews(userId, page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(newsRepository, times(1)).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }

    @Test
    @DisplayName("페이지네이션 테스트")
    void shouldHandlePaginationCorrectly() {
        // Given
        Long userId = 1L;
        int page = 1;
        int size = 5;
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(testUser);
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenReturn(testNews);

        // When
        List<PersonalizedNewsResponse> result = personalizedNewsService.getPersonalizedNews(userId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // PageRequest가 올바른 페이지와 크기로 생성되었는지 확인
        verify(newsRepository, times(1)).findWithKeywordByKeywordsAndPlatforms(
                anySet(), 
                anySet(), 
                argThat(pageable -> pageable.getPageNumber() == page && pageable.getPageSize() == size)
        );
    }

    @Test
    @DisplayName("사용자 조회 실패 시 예외 발생 테스트")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        Long userId = 999L;
        int page = 0;
        int size = 10;
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> personalizedNewsService.getPersonalizedNews(userId, page, size)
        );
        
        assertEquals("개인화된 뉴스 조회 실패", exception.getMessage());
        verify(newsKeywordRepository, never()).findAll();
        verify(newsRepository, never()).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }

    @Test
    @DisplayName("뉴스 키워드 조회 실패 시 예외 발생 테스트")
    void shouldThrowExceptionWhenNewsKeywordRepositoryFails() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(testUser);
        when(newsKeywordRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> personalizedNewsService.getPersonalizedNews(userId, page, size)
        );
        
        assertEquals("개인화된 뉴스 조회 실패", exception.getMessage());
        verify(newsRepository, never()).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }

    @Test
    @DisplayName("뉴스 조회 실패 시 예외 발생 테스트")
    void shouldThrowExceptionWhenNewsRepositoryFails() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(testUser);
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> personalizedNewsService.getPersonalizedNews(userId, page, size)
        );
        
        assertEquals("개인화된 뉴스 조회 실패", exception.getMessage());
    }

    @Test
    @DisplayName("키워드 매칭이 없을 때 빈 결과 반환 테스트")
    void shouldReturnEmptyResultWhenNoKeywordMatches() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        
        // 사용자 키워드와 다른 뉴스 키워드
        NewsKeyword differentKeyword = TestDataBuilder.createTestNewsKeywordWithId();
        differentKeyword.setText("다른키워드");
        List<NewsKeyword> differentNewsKeywords = Arrays.asList(differentKeyword);
        
        when(userRepository.findByUserIdWithKeywordsAndPlatforms(userId)).thenReturn(testUser);
        when(newsKeywordRepository.findAll()).thenReturn(differentNewsKeywords);
        when(newsRepository.findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class)))
                .thenReturn(Arrays.asList());

        // When
        List<PersonalizedNewsResponse> result = personalizedNewsService.getPersonalizedNews(userId, page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(newsRepository, times(1)).findWithKeywordByKeywordsAndPlatforms(anySet(), anySet(), any(Pageable.class));
    }
}
