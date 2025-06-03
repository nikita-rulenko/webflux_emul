package com.example.emulator.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация метрик для мониторинга приложения.
 * Настраивает счетчики и таймеры для отслеживания производительности.
 */
@Configuration
public class MetricsConfig {

    /**
     * Создает реестр метрик для приложения.
     * PrometheusMeterRegistry хранит метрики в формате Prometheus.
     */
    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * Счетчик входящих запросов.
     */
    @Bean
    public Counter requestCounter(MeterRegistry registry) {
        return Counter.builder("emulator.requests")
                .description("Общее количество запросов")
                .register(registry);
    }

    /**
     * Таймер для измерения времени обработки запросов.
     */
    @Bean
    public Timer responseTimer(MeterRegistry registry) {
        return Timer.builder("emulator.response.time")
                .description("Время обработки запросов")
                .publishPercentiles(0.5, 0.95, 0.99) // Публикуем перцентили для анализа
                .register(registry);
    }
}
