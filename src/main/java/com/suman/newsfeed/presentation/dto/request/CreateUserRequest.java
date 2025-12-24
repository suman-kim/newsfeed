package com.suman.newsfeed.presentation.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateUserRequest {

    private String email;
    private String password;
    private String nickname;

    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다");
        }

        // ✅ 이메일 형식 검증 추가
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다");
        }
    }

    // ✅ 이메일 형식 검증 메서드
    private boolean isValidEmail(String email) {
        // 기본적인 이메일 형식 검증
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
}
