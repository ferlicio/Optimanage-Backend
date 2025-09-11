package com.AIT.Optimanage.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskDecorator;

import com.AIT.Optimanage.Support.MdcTaskDecorator;
import com.AIT.Optimanage.Support.TenantTaskDecorator;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        TaskDecorator tenantDecorator = new TenantTaskDecorator();
        TaskDecorator mdcDecorator = new MdcTaskDecorator();
        executor.setTaskDecorator(task -> mdcDecorator.decorate(tenantDecorator.decorate(task)));
        executor.initialize();
        return executor;
    }
}

