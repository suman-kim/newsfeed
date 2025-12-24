package com.suman.newsfeed.infrastructure.security;

import com.suman.newsfeed.infrastructure.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // JWT 서명을 위한 키 생성
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    // Access Token 생성
    public String generateAccessToken(String domainId, String email, String nickname) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenValidity());

        return Jwts.builder()
                .subject(domainId)                          // 사용자 ID
                .claim("email", email)                    // 이메일
                .claim("nickname", nickname)              // 닉네임
                .claim("type", "access")                  // 토큰 타입
                .issuedAt(now)                           // 발행 시간
                .expiration(expiryDate)                  // 만료 시간
                .signWith(getSigningKey())               // 서명
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String domainId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshTokenValidity());

        return Jwts.builder()
                .subject(domainId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public String getDomainIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    // 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    // 토큰에서 닉네임 추출
    public String getNicknameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("nickname", String.class);
    }

    // 토큰 파싱
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT 토큰 파싱 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다", e);
        }
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);

            // 만료 시간 확인
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                log.warn("만료된 JWT 토큰입니다");
                return false;
            }

            // Access Token 타입 확인
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("Access Token이 아닙니다: {}", tokenType);
                return false;
            }

            return true;
        } catch (JwtException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // Refresh Token 유효성 검증
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);

            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (JwtException e) {
            log.error("Refresh Token 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // 토큰 만료 시간 조회
    public Date getExpirationFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    // 토큰 남은 시간 (밀리초)
    public long getRemainingTime(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }


    // ✅ 토큰 만료 여부만 확인 (유효성 검사 없이)
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;  // 만료됨
        } catch (JwtException e) {
            log.error("토큰 파싱 오류: {}", e.getMessage());
            return true;  // 오류 시 만료된 것으로 처리
        }
    }

    // ✅ 만료된 토큰에서도 사용자 ID 추출
    public String getDomainIdFromExpiredToken(String token) {
        try {
            // 만료되어도 Claims는 읽을 수 있음
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서 Claims 추출
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            log.error("만료된 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // ✅ Refresh Token 만료 시간 계산
    public LocalDateTime getRefreshTokenExpirationDate() {
        return LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenValidity() / 1000);
    }
}