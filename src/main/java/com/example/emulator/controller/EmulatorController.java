package com.example.emulator.controller;

import com.example.emulator.service.EmulatorService;
import com.fasterxml.jackson.databind.JsonNode;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST контроллер для эмулятора.
 * @RestController - говорит Spring, что этот класс обрабатывает HTTP запросы
 * @RequestMapping("/api/v1") - все URL методов будут начинаться с /api/v1
 */
@RestController
@RequestMapping("/api/v1")
//@Tag(name = "Emulator API", description = "API эмулятора с настраиваемой задержкой")
public class EmulatorController {
    private final EmulatorService emulatorService;

    /**
     * Конструктор с внедрением зависимости EmulatorService
     * Spring автоматически предоставит нужный экземпляр сервиса
     */
    public EmulatorController(EmulatorService emulatorService) {
        this.emulatorService = emulatorService;
    }

    /**
     * Метод обработки HTTP GET запроса к /api/v1/emulate
     * 
     * @GetMapping - Spring аннотация, которая говорит:
     * "Когда придет GET запрос на URL /api/v1/emulate, вызови этот метод"
     * 
     * Возвращает Mono<JsonNode> - реактивную обертку над JSON,
     * что позволяет Spring WebFlux обрабатывать ответ асинхронно
     */
    @GetMapping("/emulate")
//    @Operation(
//        summary = "Получить эмулированный ответ",
//        description = "Возвращает JSON из файла конфигурации с настраиваемой задержкой"
//    )
    public Mono<JsonNode> emulate() {
        return emulatorService.getEmulatedResponse();
    }
}
