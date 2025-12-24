package com.suman.newsfeed.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 메모리 최적화를 위해 스레드 풀 크기 조정
        executor.setCorePoolSize(5);  // 크롤링 동시 실행 수 제한
        executor.setMaxPoolSize(15);  // 최대 스레드 수 감소
        executor.setQueueCapacity(50); // 대기열 크기 감소
        executor.setThreadNamePrefix("news-collector-");
        executor.setKeepAliveSeconds(60); // 유휴 스레드 생존 시간 설정
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(30); // 종료 대기 시간
        executor.initialize();
        return executor;
    }

    @Bean("eventExecutor")
    public TaskExecutor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); 
        executor.setMaxPoolSize(10); 
        executor.setQueueCapacity(50); 
        executor.setThreadNamePrefix("event-executor-"); 
        executor.initialize();
        return executor;
    }

}
