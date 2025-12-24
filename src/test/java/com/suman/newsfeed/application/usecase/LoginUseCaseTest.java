package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.infrastructure.security.JwtTokenProvider;
import com.suman.newsfeed.presentation.dto.request.LoginRequest;
import com.suman.newsfeed.presentation.dto.response.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LoginUseCase 테스트 클래스
 * 사용자 로그인 비즈니스 로직에 대한 종합적인 테스트를 수행합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase 테스트")
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private LoginRequest validLoginRequest;
    private LoginRequest invalidLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 유효한 로그인 요청
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // 유효하지 않은 로그인 요청
        invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setEmail("invalid-email");
        invalidLoginRequest.setPassword("");

        // 테스트용 사용자
        testUser = User.create("test@example.com", "encodedPassword123", "테스트유저");
    }

    @Test
    @DisplayName("정상적인 로그인 테스트")
    void shouldLoginSuccessfully() {
        // Given
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshTokenExpirationDate()).thenReturn(expiresAt);
        doNothing().when(userRepository).save(any(User.class));

        // When
        LoginResponse response = loginUseCase.execute(validLoginRequest);

        // Then
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(testUser.getDomainId(), response.getAggregateId());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getNickname(), response.getNickname());

        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(validLoginRequest.getPassword(), testUser.getPassword());
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser.getDomainId(), testUser.getEmail(), testUser.getNickname());
        verify(jwtTokenProvider, times(1)).generateRefreshToken(testUser.getDomainId());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생 테스트")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Given
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginUseCase.execute(validLoginRequest)
        );

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생 테스트")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginUseCase.execute(validLoginRequest)
        );

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validLoginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(validLoginRequest.getPassword(), testUser.getPassword());
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void shouldGenerateJwtTokensCorrectly() {
        // Given
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser.getDomainId(), testUser.getEmail(), testUser.getNickname())).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(testUser.getDomainId())).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshTokenExpirationDate()).thenReturn(expiresAt);
        doNothing().when(userRepository).save(any(User.class));

        // When
        LoginResponse response = loginUseCase.execute(validLoginRequest);

        // Then
        verify(jwtTokenProvider, times(1)).generateAccessToken(testUser.getDomainId(), testUser.getEmail(), testUser.getNickname());
        verify(jwtTokenProvider, times(1)).generateRefreshToken(testUser.getDomainId());
        verify(jwtTokenProvider, times(1)).getRefreshTokenExpirationDate();
    }

    @Test
    @DisplayName("리프레시 토큰 업데이트 테스트")
    void shouldUpdateRefreshTokenInUser() {
        // Given
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshTokenExpirationDate()).thenReturn(expiresAt);
        doNothing().when(userRepository).save(any(User.class));

        // When
        loginUseCase.execute(validLoginRequest);

        // Then
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("사용자 저장 실패 시 예외 발생 테스트")
    void shouldThrowExceptionWhenUserSaveFails() {
        // Given
        String accessToken = "accessToken123";
        String refreshToken = "refreshToken123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        when(userRepository.findByEmail(validLoginRequest.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), anyString())).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtTokenProvider.getRefreshTokenExpirationDate()).thenReturn(expiresAt);
        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginUseCase.execute(validLoginRequest));
    }
}
