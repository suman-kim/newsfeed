package com.suman.newsfeed.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String domainId; // 사용자 고유 ID
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String domainId, String email, String nickname,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.domainId = domainId;
        this.email = email;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return email;  // 이메일을 username으로 사용
    }

    @Override
    public String getPassword() {
        return null;  // JWT에서는 비밀번호 반환하지 않음
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}