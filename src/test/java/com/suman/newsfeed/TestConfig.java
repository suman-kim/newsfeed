package com.suman.newsfeed;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트 환경을 위한 공통 설정 클래스
 * 실제 애플리케이션과 동일한 빈들을 테스트용으로 제공합니다.
 */
@TestConfiguration
public class TestConfig {

    /**
     * 테스트용 패스워드 인코더
     * BCrypt 알고리즘을 사용하여 패스워드를 해시화합니다.
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
