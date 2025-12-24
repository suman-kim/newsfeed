package com.suman.newsfeed.infrastructure.security;

import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

    private final UserRepository userRepository;

    // 사용자 ID로 UserPrincipal 생성
    public UserPrincipal loadUserByDomainId(String domainId) {
        User user = userRepository.findByDomainId(domainId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + domainId);
        }

        return new UserPrincipal(
                user.getId(),
                user.getDomainId(),
                user.getEmail(),
                user.getNickname(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // 이메일로 UserPrincipal 생성 (로그인용)
    public UserPrincipal loadUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email);
        }


        return new UserPrincipal(
                user.getId(),
                user.getDomainId(),
                user.getEmail(),
                user.getNickname(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
