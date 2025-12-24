package com.suman.newsfeed.infrastructure.database.jpa.repositories;

import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsJpaEntity;
import com.suman.newsfeed.infrastructure.database.jpa.entities.NewsKeywordJpaEntity;
import com.suman.newsfeed.infrastructure.external.crawler.NewsPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface NewsJpaRepository extends JpaRepository<NewsJpaEntity, Long> {



    /**
     * 키워드 집합과 플랫폼 집합에 모두 속하는 뉴스를
     * 생성일자 기준 내림차순으로 페이징하여 조회합니다.
     * @param keywords 뉴스 키워드 엔티티 집합
     * @param platforms 뉴스 플랫폼 집합
     * @param pageable 페이징 정보
     * @return 페이징 처리된 뉴스 엔티티
     */
    @Query("SELECT n FROM NewsJpaEntity n JOIN FETCH n.newsKeyword nk " +
            "WHERE nk IN :keywords AND n.platform IN :platforms " +
            "ORDER BY n.createdAt DESC")
    Page<NewsJpaEntity> findWithKeywordByKeywordsAndPlatforms(
            @Param("keywords") Set<NewsKeywordJpaEntity> keywords,
            @Param("platforms") Set<NewsPlatform> platforms,
            Pageable pageable
    );
}
