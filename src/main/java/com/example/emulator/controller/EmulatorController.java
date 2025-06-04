package com.example.emulator.controller;

import com.example.emulator.service.EmulatorService;
import com.example.emulator.dto.EmulatorResponse;
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

    /**
     * Конструктор с внедрением зависимостей.
     * Инициализирует сервис и метрики для мониторинга.
     *
     * @param emulatorService - сервис эмулятора
     */
    public EmulatorController(EmulatorService emulatorService) {
        this.emulatorService = emulatorService;
        log.info("Контроллер инициализирован");
    }

    /**
     * Обрабатывает GET-запросы на /api/v1/emulate.
     * Измеряет время ответа и считает количество запросов.
     *
     * @return Mono<JsonNode> - реактивный ответ в формате JSON
     */
    @GetMapping("/emulate")
    public Mono<EmulatorResponse> getEmulatedResponse() {
        return emulatorService.getEmulatedResponse();
    }
}
