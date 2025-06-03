/**
 * Архитектура проекта Spring WebFlux Emulator:
 *
 * 1. Порядок выполнения и связи компонентов:
 *    - Main.java: точка входа, запускает Spring Boot приложение
 *    - EmulatorController: REST контроллер, принимает HTTP запросы
 *    - EmulatorService (этот класс): бизнес-логика, загрузка JSON и эмуляция задержки
 *    - EmulatorConfig: конфигурация из application.yml
 *
 * 2. Service слой в WebFlux:
 *    - @Service - стандартная аннотация Spring для бизнес-логики
 *    - Этот слой НЕ является специфичным для WebFlux
 *    - Но использует реактивные типы (Mono) для асинхронной обработки
 *
 * 3. Ключевые компоненты:
 *    - ResourceLoader: Spring-компонент для загрузки файлов из classpath/файловой системы
 *    - ObjectMapper: Jackson-компонент для работы с JSON
 *    - Mono: реактивный тип WebFlux для асинхронной обработки одиночного значения
 */

package com.example.emulator.service;

import com.example.emulator.config.EmulatorConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
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
     * Возвращает JSON-ответ с эмулированной задержкой.
     * Использует реактивный подход с Mono для неблокирующей обработки.
     *
     * @return Mono<JsonNode> - реактивная обертка над JSON ответом
     */
    public Mono<JsonNode> getEmulatedResponse() {
        Duration delay = getRandomDelay();
        JsonNode response = generateResponse();
        log.debug("Запрос получен, ответ будет отправлен через: {} мс", delay.toMillis());
        
        return Mono.just(response)
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

    /**
     * Генерирует динамический JSON-ответ.
     * Включает текущее время и статус.
     *
     * @return JsonNode - сгенерированный JSON-ответ
     */
    private JsonNode generateResponse() {
        var response = new ObjectNode(JsonNodeFactory.instance)
            .put("timestamp", System.currentTimeMillis())
            .put("status", "success")
            .put("message", "Response generated at: " + LocalDateTime.now());
        return response;
    }
}
