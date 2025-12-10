package com.tripjoy.api.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for handling asynchronous event listeners.
 * This enables @Async annotation to work properly.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Số thread chạy liên tục
        executor.setCorePoolSize(5);

        // Số thread tối đa khi busy
        executor.setMaxPoolSize(10);

        // Queue size - task chờ xử lý
        executor.setQueueCapacity(100);

        // Prefix cho thread name (dễ debug log)
        executor.setThreadNamePrefix("async-");

        // Cho phép core threads timeout khi idle
        executor.setAllowCoreThreadTimeOut(true);

        // Timeout = 60s
        executor.setKeepAliveSeconds(60);

        executor.initialize();

        log.info("Async task executor configured: corePoolSize=5, maxPoolSize=10, queueCapacity=100");

        return executor;
    }
}
