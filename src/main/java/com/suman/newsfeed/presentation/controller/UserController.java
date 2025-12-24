package com.suman.newsfeed.presentation.controller;

import com.suman.newsfeed.application.usecase.CreateUserUseCase;
import com.suman.newsfeed.application.usecase.LoginUseCase;
import com.suman.newsfeed.presentation.dto.ApiResponse;
import com.suman.newsfeed.presentation.dto.request.CreateUserRequest;
import com.suman.newsfeed.presentation.dto.request.LoginRequest;

import com.suman.newsfeed.presentation.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final CreateUserUseCase createUserUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@RequestBody CreateUserRequest request) {
        createUserUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success("사용자 등록이 완료되었습니다"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);

        ApiResponse<LoginResponse> apiResponse = ApiResponse.success(response, "로그인이 완료되었습니다");
        return ResponseEntity.ok(apiResponse);
    }
}
