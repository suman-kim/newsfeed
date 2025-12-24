package com.suman.newsfeed.application.usecase;

import java.util.concurrent.CompletableFuture;

public interface NewsCollectionUseCase {
    CompletableFuture<Void> collectNewsAll();
    CompletableFuture<Void> collectNewsForKeyword(String keyword);
}
