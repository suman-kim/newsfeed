package com.suman.newsfeed.infrastructure.database.jpa.entities;


import com.suman.newsfeed.shared.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name= "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity extends BaseEntity{

    @NotBlank(message = "이메일은 필수입니다")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserKeywordJpaEntity> userKeywords = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserNewsPlatformEntity> userNewsPlatforms = new HashSet<>();

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    // ✅ Mapper에서 사용할 생성자
    public UserJpaEntity(Long id, String domainId, String email, String password, String nickname, UserRole role, String refreshToken, LocalDateTime refreshTokenExpiresAt) {
        this.id = id;
        this.domainId = domainId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.userKeywords = new HashSet<>();
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;

    }

}
