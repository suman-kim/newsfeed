package com.suman.newsfeed.presentation.controller;

import com.suman.newsfeed.application.usecase.PersonalizedNewsUseCase;
import com.suman.newsfeed.infrastructure.security.UserPrincipal;
import com.suman.newsfeed.presentation.dto.ApiResponse;
import com.suman.newsfeed.presentation.dto.response.PersonalizedNewsResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class PersonalizedNewsController {
    private static final Logger logger = LoggerFactory.getLogger(PersonalizedNewsController.class);

    private final PersonalizedNewsUseCase personalizedNewsUseCase;

    /**
     * 사용자 맞춤 뉴스 조회
     * @param userPrincipal
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/personalized")
    public ResponseEntity<ApiResponse<List<PersonalizedNewsResponse>>> getPersonalizedNews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        logger.info("개인화된 뉴스 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", userPrincipal.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(personalizedNewsUseCase.getPersonalizedNews(userPrincipal.getId(),page,size)));
    }
}
