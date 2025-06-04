/**
 * Сервисный слой эмулятора.
 *
 * Ключевые моменты:
 * 1. Архитектура:
 *    - @Service - стандартная аннотация Spring для бизнес-логики
 *    - Отвечает за генерацию ответа с задержкой
 *
 * 2. Ключевые компоненты:
 *    - EmulatorConfig: настройки задержки из application.yml
 *    - EmulatorResponse: DTO для формирования ответа
 *    - Mono: реактивный тип для асинхронной обработки
 */

package com.example.emulator.service;

import com.example.emulator.config.EmulatorConfig;
import com.example.emulator.dto.EmulatorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

/**
 * Сервис, реализующий бизнес-логику эмулятора.
 * Генерирует динамический JSON-ответ с заданной задержкой.
 */
@Service
public class EmulatorService {
    private static final Logger log = LoggerFactory.getLogger(EmulatorService.class);
    
    // Генератор случайных чисел для задержки
    private final Random random;
    
    // Минимальная задержка из конфигурации
    private final int minDelay;
    
    // Максимальная задержка из конфигурации
    private final int maxDelay;
    
    // Диапазон задержки (кэшируется для оптимизации)
    private final int delayRange;

    /**
     * Конструктор сервиса с внедрением зависимостей (Dependency Injection).
     * Инициализирует все необходимые поля и вычисляет диапазон задержки.
     *
     * @param config - конфигурация из application.yml
     */
    public EmulatorService(EmulatorConfig config) {
        this.random = new Random();
        this.minDelay = config.getDelay().getMin();
        this.maxDelay = config.getDelay().getMax();
        // Кэшируем разницу для оптимизации генерации случайных чисел
        this.delayRange = maxDelay - minDelay + 1;
        log.info("Сервис инициализирован с задержкой {} - {} мс", minDelay, maxDelay);
    }

    /**
     * Генерирует ответ с заданной задержкой.
     * Задержка выбирается случайным образом из заданного диапазона.
     *
     * @return Mono<EmulatorResponse> - реактивная обертка над DTO ответом
     */
    public Mono<EmulatorResponse> getEmulatedResponse() {
        Duration delay = getRandomDelay();
        log.debug("Запрос получен, ответ будет отправлен через: {} мс", delay.toMillis());
        
        return Mono.just(EmulatorResponse.createResponse())
                .delayElement(delay);
    }

    /**
     * Генерирует случайную задержку в заданном диапазоне.
     * Использует кэшированный диапазон для оптимизации.
     *
     * @return Duration - объект длительности для WebFlux
     */
    private Duration getRandomDelay() {
        return Duration.ofMillis(random.nextInt(delayRange) + minDelay);
    }


}
