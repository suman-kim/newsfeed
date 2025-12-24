package com.suman.newsfeed.presentation.controller;


import com.suman.newsfeed.application.usecase.AddUserNewsPlatformUseCase;
import com.suman.newsfeed.application.usecase.GetUserNewsPlatformsUseCase;
import com.suman.newsfeed.domain.user.UserNewsPlatform;
import com.suman.newsfeed.infrastructure.security.UserPrincipal;
import com.suman.newsfeed.presentation.dto.ApiResponse;
import com.suman.newsfeed.presentation.dto.request.SubscribeNewsPlatformRequest;
import com.suman.newsfeed.presentation.dto.response.UserNewsPlatformResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user-news-platforms")
//사용자의 뉴스 플랫폼 구독
public class UserNewsPlatformController {

    private final AddUserNewsPlatformUseCase addUserNewsPlatformUseCase;
    private final GetUserNewsPlatformsUseCase getUserNewsPlatformsUseCase;

    //뉴스 플랫폼 구독
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeNewsPlatform(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody SubscribeNewsPlatformRequest request) {
        addUserNewsPlatformUseCase.execute(userPrincipal.getId(), request.getNewsPlatform());
        return ResponseEntity.ok(ApiResponse.success("뉴스 플랫폼 구독이 완료되었습니다"));
    }

    //사용자가 구독한 플랫폼 조회
    @GetMapping("/my-platforms")
    public ResponseEntity<ApiResponse<UserNewsPlatformResponse>> getMyNewsPlatforms(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<UserNewsPlatform> platforms = getUserNewsPlatformsUseCase.execute(userPrincipal.getId());

        // NewsPlatform enum에서 플랫폼명만 추출하여 String 리스트로 변환
        List<String> platformNames = platforms.stream()
                .map(userNewsPlatform -> userNewsPlatform.getNewsPlatform().name()) // enum을 String으로 변환
                .collect(Collectors.toList());

        UserNewsPlatformResponse response = UserNewsPlatformResponse.builder()
                .platform(platformNames)
                .build();


        return ResponseEntity.ok(ApiResponse.success(response, "구독 중인 뉴스 플랫폼 목록입니다"));
    }

}
