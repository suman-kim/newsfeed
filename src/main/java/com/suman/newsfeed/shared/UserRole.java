package com.suman.newsfeed.shared;


import lombok.Getter;

@Getter
public enum UserRole {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String role;
    private final String title;

    UserRole(String role, String title) {
        this.role = role;
        this.title = title;
    }
}
