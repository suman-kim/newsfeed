package com.suman.newsfeed;

import com.suman.newsfeed.application.usecase.NewsCollectionUseCase;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class NewsfeedApplication {
	public static void main(String[] args) {
		SpringApplication.run(NewsfeedApplication.class, args);
	}



	@PostConstruct
	public void init(){
		// Spring 컨텍스트가 모두 로드된 후에 실행됩니다
		System.out.println("=== 테스트 시작 ===");

	}

}