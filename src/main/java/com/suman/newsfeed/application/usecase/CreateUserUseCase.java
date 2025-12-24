package com.suman.newsfeed.application.usecase;


import com.suman.newsfeed.application.event.DomainEventPublisher;
import com.suman.newsfeed.domain.user.User;
import com.suman.newsfeed.domain.user.UserRepository;
import com.suman.newsfeed.presentation.dto.request.CreateUserRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DomainEventPublisher domainEventPublisher;

    public void execute(CreateUserRequest userRequest) {
        log.info("사용자 생성 시작: email={}", userRequest.getEmail());
        log.info("사용자 생성 요청: {}", userRequest);


        // 요청 유효성 검사
        userRequest.validate();

        //이메일 중복 검사
        validateEmailNotExists(userRequest.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

        // 사용자 생성
        User user = User.create(userRequest.getEmail(), encodedPassword, userRequest.getNickname());
        log.info("사용자 생성: email={}, nickname={}", user.getEmail(), user.getNickname());

        // 사용자 엔티티 저장
        userRepository.save(user);
        // 도메인 이벤트 발행
        domainEventPublisher.publishEvents(user);
    }


    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + email);
        }
    }
}
