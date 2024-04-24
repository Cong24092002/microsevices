package com.microservices.reviewservice.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@ComponentScan("com")
public class ReactiveDBConfig {
    @Value("${app.threadPoolSize}")
    private final Integer threadPoolSize;
    @Value("${app.taskQueueSize}")
    private final Integer taskQueueSize;
    @Autowired
    public ReactiveDBConfig(Integer threadPoolSize, Integer taskQueueSize) {
        this.threadPoolSize = threadPoolSize;
        this.taskQueueSize = taskQueueSize;
    }

    @Bean
    public Scheduler jdbcScheduler(){
        return Schedulers.newBoundedElastic(threadPoolSize,taskQueueSize,"jdbc-pool");
    }

}
