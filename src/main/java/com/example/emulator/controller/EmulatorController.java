package com.example.emulator.controller;

import com.example.emulator.service.EmulatorService;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST контроллер для эмулятора.
 * Обрабатывает входящие HTTP-запросы и собирает метрики.
 */
@RestController
@RequestMapping("/api/v1")
public class EmulatorController {
    private static final Logger log = LoggerFactory.getLogger(EmulatorController.class);
    
    private final EmulatorService emulatorService;
    private final Counter requestCounter;
    private final Timer responseTimer;

    /**
     * Конструктор с внедрением зависимостей.
     * Инициализирует сервис и метрики для мониторинга.
     *
     * @param emulatorService - сервис эмулятора
     * @param requestCounter - счетчик запросов
     * @param responseTimer - таймер времени ответа
     */
    public EmulatorController(EmulatorService emulatorService, 
                             Counter requestCounter,
                             Timer responseTimer) {
        this.emulatorService = emulatorService;
        this.requestCounter = requestCounter;
        this.responseTimer = responseTimer;
        log.info("Контроллер инициализирован с метриками");
    }

    /**
     * Обрабатывает GET-запросы на /api/v1/emulate.
     * Измеряет время ответа и считает количество запросов.
     *
     * @return Mono<JsonNode> - реактивный ответ в формате JSON
     */
    @GetMapping("/emulate")
    public Mono<JsonNode> getEmulatedResponse() {
        // Увеличиваем счетчик запросов
        requestCounter.increment();
        
        // Замеряем время обработки запроса
        return responseTimer.record(() -> emulatorService.getEmulatedResponse());
    }
}
