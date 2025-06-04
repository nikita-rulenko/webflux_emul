package com.example.emulator.config;

import io.micrometer.core.instrument.MeterRegistry;
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
}
