package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.news.NewsRepository;
import com.suman.newsfeed.domain.user.UserNews;
import com.suman.newsfeed.domain.user.UserNewsRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.UserNewsJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.NewsJpaRepository;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserJpaRepository;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.UserNewsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserNewsRepositoryAdapter implements UserNewsRepository {
    private final UserNewsJpaRepository userNewsJpaRepository;
    private final NewsJpaRepository newsJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(UserNews userNews){
        NewsJpaEntity newsJpaEntity = newsJpaRepository.findById(userNews.getNews().getId()).orElseThrow(()
                -> new IllegalArgumentException("없는 뉴스 : " + userNews.getNews().getId()));

        UserJpaEntity userJpaEntity = userJpaRepository.findById(userNews.getUser().getId()).orElseThrow(()
                -> new IllegalArgumentException("없는 사용자 : " + userNews.getUser().getId()));


        UserNewsJpaEntity userNewsJpaEntity = new UserNewsJpaEntity(userNews.getId(),userJpaEntity,newsJpaEntity, userNews.getIsRead());
        userNewsJpaRepository.save(userNewsJpaEntity);

    }
}
