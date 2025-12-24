package com.suman.newsfeed.infrastructure.database.jpa.adapters;

import com.suman.newsfeed.domain.news.NewsKeyword;
import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsKeywordJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.repositories.NewsKeywordJpaRepository;
import com.suman.newsfeed.infrastructure.mappers.NewsKeywordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Repository
public class NewsKeywordRepositoryAdapter implements NewsKeywordRepository {
    private final NewsKeywordJpaRepository newsKeywordJpaRepository;
    private final NewsKeywordMapper newsKeywordMapper;

    @Override
    @Transactional
    public void save(NewsKeyword newsKeyword){
        NewsKeywordJpaEntity newsKeywordJpaEntity = new NewsKeywordJpaEntity(newsKeyword.getId(),
                                                                                newsKeyword.getDomainId(),
                                                                                newsKeyword.getText(),
                                                                                newsKeyword.getCollectedCount());
        newsKeywordJpaRepository.save(newsKeywordJpaEntity);
    }

    @Override
    @Transactional
    public void update(NewsKeyword newsKeyword){
        NewsKeywordJpaEntity newsKeywordJpaEntity = newsKeywordJpaRepository.findById(newsKeyword.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 뉴스 키워드를 찾을 수 없습니다: "));

        newsKeywordJpaEntity.update(newsKeyword);
    }


    @Override
    @Transactional(readOnly = true)
    public List<NewsKeyword> findAll(){
        return newsKeywordMapper.toDomainList(newsKeywordJpaRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllTextList(){
        return newsKeywordJpaRepository.findAll()
                .stream()
                .map(NewsKeywordJpaEntity::getText)
                .toList();
    }

    @Override
    public boolean existsByText(String text) {
        return newsKeywordJpaRepository.existsByText(text);
    }

    @Override
    @Transactional(readOnly = true)
    public NewsKeyword findByText(String text){
        NewsKeywordJpaEntity newsKeywordJpaEntity = newsKeywordJpaRepository.findByText(text);
        return newsKeywordMapper.toDomain(newsKeywordJpaEntity);
    }

    @Override
    @Transactional
    public void deleteAllByTexts (List<String> textList){
        newsKeywordJpaRepository.deleteAllByTextIn(textList);
    }

}
