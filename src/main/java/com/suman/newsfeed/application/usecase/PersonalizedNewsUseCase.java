package com.suman.newsfeed.application.usecase;

import com.suman.newsfeed.presentation.dto.response.PersonalizedNewsResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PersonalizedNewsUseCase {
    List<PersonalizedNewsResponse> getPersonalizedNews(Long userId, int page, int size);
}