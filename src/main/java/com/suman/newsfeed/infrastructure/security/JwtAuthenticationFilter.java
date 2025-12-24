package com.suman.newsfeed.infrastructure.security;

import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("🔐 JWT 인증 필터 실행");
        log.info("📋 Authorization 헤더: {}", request.getHeader("Authorization"));

        try {
            log.info("🔍 요청에서 Access Token 추출 중...");
            String accessToken = extractTokenFromRequest(request);

            log.debug("🔍 요청에서 Access Token 추출: {}", accessToken);

            if (StringUtils.hasText(accessToken)) {

                // ✅ 1. Access Token이 유효한 경우 → 정상 처리
                if (jwtTokenProvider.validateToken(accessToken)) {
                    setAuthentication(accessToken);
                    log.debug("✅ Access Token 인증 성공");
                }
                // ✅ 2. Access Token이 만료된 경우 → Refresh Token으로 갱신 시도
                else if (jwtTokenProvider.isTokenExpired(accessToken)) {
                    log.info("⏰ Access Token 만료됨. Refresh Token으로 갱신 시도");

                    String newAccessToken = tryRefreshToken(accessToken, response);
                    if (newAccessToken != null) {
                        setAuthentication(newAccessToken);
                        log.info("🔄 Access Token 자동 갱신 성공");
                    } else {
                        log.warn("❌ Refresh Token 갱신 실패");
                    }
                }
                // ✅ 3. Access Token이 유효하지 않은 경우 → 인증 실패
                else {
                    log.warn("❌ 유효하지 않은 Access Token");
                }
            }
        }
        catch (Exception e) {
            log.error("💥 JWT 인증 처리 중 오류 발생", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    // ✅ Refresh Token으로 새 Access Token 발급 시도
    private String tryRefreshToken(String expiredAccessToken, HttpServletResponse response) {
        try {
            // 1. 만료된 Access Token에서 사용자 ID 추출
            String domainId = jwtTokenProvider.getDomainIdFromExpiredToken(expiredAccessToken);
            if (domainId == null) {
                return null;
            }

            // 2. 사용자의 Refresh Token 조회
            User user = userRepository.findByDomainId(domainId);
            if (user == null) {
                return null;
            }

            String storedRefreshToken = user.getRefreshToken();
            if (storedRefreshToken == null || user.isRefreshTokenExpired()) {
                log.warn("❌ Refresh Token이 없거나 만료됨: userId={}", domainId);
                return null;
            }

            // 3. Refresh Token 유효성 검증
            if (!jwtTokenProvider.validateRefreshToken(storedRefreshToken)) {
                log.warn("❌ 유효하지 않은 Refresh Token: userId={}", domainId);

                user.clearRefreshToken(); // Refresh Token 무효화
                userRepository.save(user);

                return null;
            }

            // 4. 새로운 Access Token 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(
                    user.getDomainId(),
                    user.getEmail(),
                    user.getNickname()
            );

            // 5. 새로운 Refresh Token 생성 (보안상 권장)
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getDomainId());
            LocalDateTime newExpiresAt = jwtTokenProvider.getRefreshTokenExpirationDate();

            user.updateRefreshToken(newRefreshToken, newExpiresAt);
            userRepository.save(user);

            // ✅ 6. 응답 헤더에 새 Access Token 추가
            response.setHeader("X-New-Access-Token", newAccessToken);
            response.setHeader("X-New-Refresh-Token", newRefreshToken);

            log.info("🎯 새 토큰 발급 완료: userId={}", domainId);
            return newAccessToken;

        }
        catch (Exception e) {
            log.error("💥 Refresh Token 처리 중 오류", e);
            return null;
        }
    }

    // 인증 정보 설정
    private void setAuthentication(String accessToken) {
        String domainId = jwtTokenProvider.getDomainIdFromToken(accessToken);
        UserPrincipal userPrincipal = userDetailsService.loadUserByDomainId(domainId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // ✅ 자세한 디버깅 로그 추가
        String bearerToken = request.getHeader("Authorization");

        log.info("📋 원본 Authorization 헤더: '{}'", bearerToken);
        log.info("📋 헤더 길이: {}", bearerToken != null ? bearerToken.length() : "null");

        if (bearerToken != null) {
            log.info("📋 헤더 앞 10글자: '{}'", bearerToken.length() > 10 ? bearerToken.substring(0, 10) : bearerToken);
        }

        if (StringUtils.hasText(bearerToken)) {
            log.info("✅ bearerToken에 텍스트 있음");

            if (bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                log.info("✅ Bearer 토큰 추출 성공, 토큰 길이: {}", token.length());
                log.info("🎫 추출된 토큰 앞 20글자: {}", token.length() > 20 ? token.substring(0, 20) + "..." : token);
                return token;
            } else {
                log.warn("❌ Bearer 접두사가 없음. 실제 헤더: '{}'", bearerToken);
            }
        } else {
            log.warn("❌ Authorization 헤더가 비어있거나 null");
        }

        return null;
    }
}