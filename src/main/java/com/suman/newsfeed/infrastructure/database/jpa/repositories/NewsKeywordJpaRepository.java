package com.suman.newsfeed.infrastructure.database.jpa.repositories;


import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsKeywordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsKeywordJpaRepository extends JpaRepository<NewsKeywordJpaEntity, Long> {
    boolean existsByText(String text);
    NewsKeywordJpaEntity findByText(String text);
    void deleteAllByTextIn(Iterable<String> texts);
}
