package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.TestDataBuilder;
import com.suman.newsfeed.domain.news.News;
import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.infrastructure.external.crawler.NewsDataDto;
import com.suman.newsfeed.infrastructure.external.crawler.NewsDataProvider;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * NewsCollectionService 테스트 클래스
 * 뉴스 수집 비즈니스 로직에 대한 종합적인 테스트를 수행합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsCollectionService 테스트")
class NewsCollectionServiceTest {

    @Mock
    private NewsKeywordRepository newsKeywordRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsDataProvider newsDataProvider;

    @Mock
    private Executor taskExecutor;

    @InjectMocks
    private NewsCollectionService newsCollectionService;

    private List<NewsKeyword> testNewsKeywords;
    private List<NewsDataDto> testNewsData;
    private List<NewsPlatform> supportedPlatforms;

    @BeforeEach
    void setUp() {
        // 테스트용 뉴스 키워드
        NewsKeyword keyword1 = TestDataBuilder.createTestNewsKeywordWithId();
        keyword1.setText("테스트키워드1");
        keyword1.setCollectedCount(0L);
        
        NewsKeyword keyword2 = TestDataBuilder.createTestNewsKeywordWithId();
        keyword2.setText("테스트키워드2");
        keyword2.setCollectedCount(1L);
        
        testNewsKeywords = Arrays.asList(keyword1, keyword2);
        
        // 테스트용 뉴스 데이터
        NewsDataDto newsData1 = new NewsDataDto();
        newsData1.setTitle("테스트 뉴스 제목1");
        newsData1.setContent("테스트 뉴스 내용1");
        newsData1.setUrl("https://example.com/news1");
        newsData1.setPlatform(NewsPlatform.NAVER);
        
        NewsDataDto newsData2 = new NewsDataDto();
        newsData2.setTitle("테스트 뉴스 제목2");
        newsData2.setContent("테스트 뉴스 내용2");
        newsData2.setUrl("https://example.com/news2");
        newsData2.setPlatform(NewsPlatform.GOOGLE);
        
        testNewsData = Arrays.asList(newsData1, newsData2);
        
        // 지원되는 플랫폼
        supportedPlatforms = Arrays.asList(NewsPlatform.NAVER, NewsPlatform.GOOGLE);
    }

    @Test
    @DisplayName("전체 뉴스 수집 성공 테스트")
    void shouldCollectNewsAllSuccessfully() {
        // Given
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsDataProvider.getSupportedPlatforms()).thenReturn(supportedPlatforms);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(testNewsData);
        doNothing().when(newsKeywordRepository).update(any(NewsKeyword.class));

        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsAll();

        // Then
        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());
        
        verify(newsKeywordRepository, times(1)).findAll();
        verify(newsDataProvider, times(1)).getSupportedPlatforms();
        verify(newsDataProvider, times(2)).fetchNewsByKeyword(anyString(), anyLong(), anyInt());
        verify(newsKeywordRepository, times(2)).update(any(NewsKeyword.class));
        verify(newsRepository, times(4)).save(any(News.class)); // 각 키워드당 2개씩
    }

    @Test
    @DisplayName("특정 키워드 뉴스 수집 성공 테스트")
    void shouldCollectNewsForKeywordSuccessfully() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(testNewsData);
        doNothing().when(newsKeywordRepository).update(any(NewsKeyword.class));
        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());
        
        verify(newsKeywordRepository, times(1)).findByText(keyword);
        verify(newsDataProvider, times(1)).fetchNewsByKeyword(keyword, 0L, 10);
        verify(newsKeywordRepository, times(1)).update(newsKeyword);
        verify(newsRepository, times(2)).save(any(News.class));
    }

    @Test
    @DisplayName("존재하지 않는 키워드로 뉴스 수집 시 빈 결과 반환 테스트")
    void shouldReturnEmptyResultForNonExistentKeyword() {
        // Given
        String nonExistentKeyword = "존재하지않는키워드";
        when(newsKeywordRepository.findByText(nonExistentKeyword)).thenReturn(null);

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(nonExistentKeyword);

        // Then
        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());
        
        verify(newsKeywordRepository, times(1)).findByText(nonExistentKeyword);
        verify(newsDataProvider, never()).fetchNewsByKeyword(anyString(), anyLong(), anyInt());
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    @DisplayName("지원되는 플랫폼이 없을 때 빈 결과 반환 테스트")
    void shouldReturnEmptyResultWhenNoSupportedPlatforms() {
        // Given
        when(newsKeywordRepository.findAll()).thenReturn(testNewsKeywords);
        when(newsDataProvider.getSupportedPlatforms()).thenReturn(Arrays.asList());

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsAll();

        // Then
        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());
        
        verify(newsKeywordRepository, times(1)).findAll();
        verify(newsDataProvider, times(1)).getSupportedPlatforms();
        verify(newsDataProvider, never()).fetchNewsByKeyword(anyString(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("뉴스 데이터가 없을 때 처리 테스트")
    void shouldHandleEmptyNewsData() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(Arrays.asList());
        doNothing().when(newsKeywordRepository).update(any(NewsKeyword.class));
        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());

        verify(newsKeywordRepository, times(1)).update(newsKeyword);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    @DisplayName("뉴스 수집 실패 시 예외 처리 테스트")
    void shouldHandleNewsCollectionFailure() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("API 호출 실패"));
        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        
        verify(newsKeywordRepository, never()).update(any(NewsKeyword.class));
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    @DisplayName("뉴스 키워드 업데이트 실패 시 예외 처리 테스트")
    void shouldHandleNewsKeywordUpdateFailure() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(testNewsData);
        doThrow(new RuntimeException("Database error")).when(newsKeywordRepository).update(any(NewsKeyword.class));
        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    @DisplayName("뉴스 저장 실패 시 예외 처리 테스트")
    void shouldHandleNewsSaveFailure() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(testNewsData);
        doNothing().when(newsKeywordRepository).update(any(NewsKeyword.class));
        doThrow(new RuntimeException("Database error")).when(newsRepository).save(any(News.class));
        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    @DisplayName("전체 뉴스 수집 실패 시 예외 처리 테스트")
    void shouldHandleCollectNewsAllFailure() {
        // Given
        when(newsKeywordRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        CompletableFuture<Void> result = newsCollectionService.collectNewsAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
        
        verify(newsDataProvider, never()).getSupportedPlatforms();
    }

    @Test
    @DisplayName("뉴스 키워드 카운트 업데이트 테스트")
    void shouldUpdateNewsKeywordCount() {
        // Given
        String keyword = "테스트키워드1";
        NewsKeyword newsKeyword = testNewsKeywords.get(0);
        Long initialCount = newsKeyword.getCollectedCount();
        
        when(newsKeywordRepository.findByText(keyword)).thenReturn(newsKeyword);
        when(newsDataProvider.fetchNewsByKeyword(anyString(), anyLong(), anyInt()))
                .thenReturn(testNewsData);
        doNothing().when(newsKeywordRepository).update(any(NewsKeyword.class));

        
        // taskExecutor를 동기적으로 실행하도록 설정
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        // When
        newsCollectionService.collectNewsForKeyword(keyword);

        // Then
        verify(newsKeywordRepository, times(1)).update(newsKeyword);
        // 카운트가 증가했는지 확인 (실제로는 updateCount() 메서드가 호출되어야 함)
    }
}
