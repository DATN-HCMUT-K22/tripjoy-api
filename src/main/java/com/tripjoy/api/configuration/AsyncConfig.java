package com.tripjoy.api.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);       // Số thread chạy liên tục
        executor.setMaxPoolSize(50);        // Số thread tối đa khi busy
        executor.setQueueCapacity(100);     // Queue size - task chờ xử lý
        executor.setThreadNamePrefix("TripJoyAsync-");
        executor.initialize();
        return executor;
    }
}
