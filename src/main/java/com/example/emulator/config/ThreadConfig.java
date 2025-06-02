package com.example.emulator.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

@Configuration
public class ThreadConfig {
    
    @Bean
    public Scheduler applicationScheduler(MeterRegistry registry) {
        Scheduler scheduler = Schedulers.newBoundedElastic(
            4, // Максимальное количество потоков
            100, // Размер очереди задач
            "app-scheduler",
            60, // TTL потоков в секундах
            true // Демон-потоки
        );

        // Добавляем метрики для потоков
        registry.gauge(
            "application.thread.pool.size",
            Collections.singletonList(Tag.of("pool", "app-scheduler")),
            scheduler,
            s -> Thread.getAllStackTraces().keySet().stream()
                    .filter(t -> t.getName().startsWith("app-scheduler"))
                    .count()
        );

        return scheduler;
    }
}
