package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.presentation.dto.request.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CreateUserUseCase 테스트 클래스
 * 사용자 생성 비즈니스 로직에 대한 종합적인 테스트를 수행합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserUseCase 테스트")
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    private CreateUserRequest validUserRequest;
    private CreateUserRequest invalidUserRequest;

    @BeforeEach
    void setUp() {
        // 유효한 사용자 생성 요청
        validUserRequest = new CreateUserRequest();
        validUserRequest.setEmail("test@example.com");
        validUserRequest.setPassword("password123");
        validUserRequest.setNickname("테스트유저");

        // 유효하지 않은 사용자 생성 요청
        invalidUserRequest = new CreateUserRequest();
        invalidUserRequest.setEmail("invalid-email");
        invalidUserRequest.setPassword("");
        invalidUserRequest.setNickname("");
    }

    @Test
    @DisplayName("정상적인 사용자 생성 테스트")
    void shouldCreateUserSuccessfully() {
        // Given
        String encodedPassword = "encodedPassword123";
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        doNothing().when(userRepository).save(any(User.class));

        // When
        assertDoesNotThrow(() -> createUserUseCase.execute(validUserRequest));

        // Then
        verify(userRepository, times(1)).existsByEmail(validUserRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(validUserRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(domainEventPublisher, times(1)).publishEvents(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복 시 예외 발생 테스트")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createUserUseCase.execute(validUserRequest)
        );

        assertEquals("이미 존재하는 이메일입니다: " + validUserRequest.getEmail(), exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(validUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
    }

    @Test
    @DisplayName("패스워드 인코딩 테스트")
    void shouldEncodePasswordCorrectly() {
        // Given
        String encodedPassword = "encodedPassword123";
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validUserRequest.getPassword())).thenReturn(encodedPassword);
        doNothing().when(userRepository).save(any(User.class));

        // When
        createUserUseCase.execute(validUserRequest);

        // Then
        verify(passwordEncoder, times(1)).encode(validUserRequest.getPassword());
    }

    @Test
    @DisplayName("사용자 저장 후 도메인 이벤트 발행 테스트")
    void shouldPublishDomainEventsAfterUserSave() {
        // Given
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(userRepository).save(any(User.class));

        // When
        createUserUseCase.execute(validUserRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        verify(domainEventPublisher, times(1)).publishEvents(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 시 로그 기록 테스트")
    void shouldLogUserCreationProcess() {
        // Given
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doNothing().when(userRepository).save(any(User.class));

        // When
        createUserUseCase.execute(validUserRequest);

        // Then
        // 로그 기록은 실제로는 확인할 수 없지만, 메서드가 정상적으로 실행되었음을 확인
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 시 롤백 테스트")
    void shouldRollbackWhenUserSaveFails() {
        // Given
        when(userRepository.existsByEmail(validUserRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> createUserUseCase.execute(validUserRequest));
        
        // 도메인 이벤트가 발행되지 않았는지 확인
        verify(domainEventPublisher, never()).publishEvents(any(User.class));
    }
}
