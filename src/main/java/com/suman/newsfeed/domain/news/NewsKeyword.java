package com.suman.newsfeed.domain.news;


import com.suman.newsfeed.domain.news.event.NewsKeywordCreatedEvent;
import com.suman.newsfeed.domain.shared.BaseAggregate;
import lombok.Data;

import java.util.UUID;

@Data
public class NewsKeyword extends BaseAggregate {
    private Long id;
    private String text;
    //수집된 횟수
    private Long collectedCount;

    private NewsKeyword(Long id, String domainId, String text, Long collectedCount) {
        super(domainId);
        this.id = id;
        this.text = text;
        this.collectedCount = collectedCount;
    }

    public static NewsKeyword create(String text) {
        String domainId = UUID.randomUUID().toString();
        NewsKeyword newsKeyword = new NewsKeyword(null, domainId, text,1L);

        System.out.println("뉴스 도메인 생성 -> : " + text);
        //도메인 이벤트 생성
        newsKeyword.addDomainEvent(new NewsKeywordCreatedEvent(domainId,text));
        return newsKeyword;

    }

    public static NewsKeyword reconstruct(Long id,String domainId, String text, Long collectedCount){
        return new NewsKeyword(id,domainId,text,collectedCount);
    }

    public void updateCount(){
        this.collectedCount = this.collectedCount + 1;
    }
}
