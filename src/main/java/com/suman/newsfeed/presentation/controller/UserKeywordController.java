package com.suman.newsfeed.presentation.controller;

import com.suman.newsfeed.application.usecase.ActiveKeyWordUseCase;
import com.suman.newsfeed.application.usecase.GetUserKeywordsUseCase;
import com.suman.newsfeed.application.usecase.SyncUserKeywordUseCase;
import com.suman.newsfeed.domain.user.UserKeyword;
import com.suman.newsfeed.infrastructure.security.UserPrincipal;
import com.suman.newsfeed.presentation.dto.ApiResponse;
import com.suman.newsfeed.presentation.dto.request.SubscribeKeywordRequest;
import com.suman.newsfeed.presentation.dto.response.UserKeywordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user-keywords")
public class UserKeywordController {

    private final SyncUserKeywordUseCase syncUserKeywordUseCase;
    private final ActiveKeyWordUseCase activeKeyWordUseCase;
    private final GetUserKeywordsUseCase getUserKeywordsUseCase;

    //키워드 구독
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeKeyword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @Valid @RequestBody SubscribeKeywordRequest request) {


        syncUserKeywordUseCase.execute(userPrincipal.getId(), request.getKeywords());
        return ResponseEntity.ok(ApiResponse.success("키워드 구독이 완료되었습니다"));
    }

    //키워드 활성화 & 해제
    @PutMapping("/active/{keywordId}")
    public ResponseEntity<ApiResponse<Void>> activeKeyword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                @PathVariable Long keywordId) {
        activeKeyWordUseCase.execute(userPrincipal.getId(), keywordId);
        return ResponseEntity.ok(ApiResponse.success("키워드 활성화/비활성화가 완료되었습니다"));

    }

    /**
     * 사용자가 구독한 키워드 목록 조회
     *
     * @param userPrincipal 인증된 사용자 정보
     * @return 구독 중인 키워드 목록
     */
    @GetMapping("/my-keywords")
    public ResponseEntity<ApiResponse<UserKeywordResponse>> getMyKeywords(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<UserKeyword> keywords = getUserKeywordsUseCase.execute(userPrincipal.getId());

        // 키워드명만 추출하여 String 리스트로 변환
        List<String> keywordNames = keywords.stream()
                .map(UserKeyword::getText) // 키워드명 추출
                .collect(Collectors.toList());

        UserKeywordResponse response = UserKeywordResponse.builder()
                .keywords(keywordNames)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "구독 중인 키워드 목록입니다"));
    }
}

