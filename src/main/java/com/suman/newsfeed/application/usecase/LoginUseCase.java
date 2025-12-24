package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.infrastructure.security.JwtTokenProvider;
import com.suman.newsfeed.presentation.dto.request.LoginRequest;
import com.suman.newsfeed.presentation.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse execute(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getDomainId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getDomainId());
        LocalDateTime newExpiresAt = jwtTokenProvider.getRefreshTokenExpirationDate();

        //refresh token 토큰 업데이트
        user.updateRefreshToken(refreshToken, newExpiresAt);
        userRepository.save(user);


        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getDomainId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}
