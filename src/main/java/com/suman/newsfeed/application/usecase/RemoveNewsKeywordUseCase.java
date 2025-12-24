package com.suman.newsfeed.application.usecase;


import com.suman.newsfeed.domain.news.NewsKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RemoveNewsKeywordUseCase {

    private final NewsKeywordRepository newsKeywordRepository;

    @Transactional
    public void execute(List<String> textList){
        newsKeywordRepository.deleteAllByTexts(textList);
    }
}
