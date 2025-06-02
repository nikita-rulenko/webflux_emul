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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import java.time.Duration;
import java.util.Random;
import java.io.IOException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * Сервис, реализующий бизнес-логику эмулятора.
 * Загружает JSON из файла и эмулирует задержку.
 */
@Service
public class EmulatorService {
    private static final Logger log = LoggerFactory.getLogger(EmulatorService.class);
    private final EmulatorConfig config;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final Scheduler applicationScheduler;

    /**
     * Конструктор сервиса с внедрением зависимостей (Dependency Injection)
     * @param config - конфигурация из application.yml
     * @param resourceLoader - Spring компонент для загрузки файлов
     * @param objectMapper - Jackson компонент для работы с JSON
     */
    public EmulatorService(EmulatorConfig config, ResourceLoader resourceLoader, ObjectMapper objectMapper, Scheduler applicationScheduler) {
        this.config = config;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.applicationScheduler = applicationScheduler;
    }
    private final Random random = new Random();

    /**
     * Возвращает JSON-ответ с эмулированной задержкой
     * @return Mono<JsonNode> - реактивный JSON-ответ
     */
    /**
     * Основной метод сервиса, который:
     * 1. Загружает JSON из файла, указанного в конфигурации
     * 2. Добавляет случайную задержку
     * 3. Логирует процесс
     * 
     * Использует реактивный тип Mono для асинхронной обработки:
     * - fromCallable: преобразует блокирующую операцию чтения файла в реактивную
     * - delayElement: добавляет задержку
     * - doOnSubscribe/doOnSuccess: добавляет логирование
     * 
     * @return Mono<JsonNode> - реактивная обертка над JSON ответом
     */
    public Mono<JsonNode> getEmulatedResponse() {
        return Mono.<JsonNode>fromCallable(() -> {
            // Загружаем JSON из файла
            var resource = resourceLoader.getResource(config.getResponseConfig());
            try (var inputStream = resource.getInputStream()) {
                return objectMapper.readTree(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при чтении JSON", e);
            }
        })
        .delayElement(getRandomDelay(), applicationScheduler)
        .subscribeOn(applicationScheduler)
        .doOnSubscribe(s -> log.debug("Запрос получен, применяется задержка: {} мс", 
            config.getDelay().getMin() + "-" + config.getDelay().getMax()))
        .doOnSuccess(j -> log.debug("Ответ отправлен"));
    }

    /**
     * Генерирует случайную задержку в заданном диапазоне
     * @return Duration - длительность задержки
     */
    /**
     * Генерирует случайную задержку в миллисекундах
     * в диапазоне от min до max из конфигурации
     * 
     * @return Duration - объект длительности для WebFlux
     */
    private Duration getRandomDelay() {
        int min = config.getDelay().getMin();
        int max = config.getDelay().getMax();
        int delay = random.nextInt(max - min + 1) + min;
        return Duration.ofMillis(delay);
    }
}
