package com.example.emulator.controller;

import com.example.emulator.dto.EmulatorResponse;
import com.example.emulator.dto.OrderRequest;
import com.example.emulator.dto.OrderResponse; // Добавлен импорт
import com.example.emulator.dto.Cpn;
import com.example.emulator.service.CpnConfigurationService;
import com.example.emulator.config.EmulatorConfig;
import com.example.emulator.service.EmulatorService;
import com.example.emulator.service.OrderResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST контроллер для эмулятора.
 * Обрабатывает входящие HTTP-запросы.
 * Метрики собираются автоматически через Spring Actuator.
 */
@RestController
@RequestMapping("/api/back/v1")
public class EmulatorController {
    private static final Logger log = LoggerFactory.getLogger(EmulatorController.class);
    
    private final EmulatorService emulatorService;
    private final CpnConfigurationService cpnConfigurationService;
    private final OrderResponseService orderResponseService;
    private final EmulatorConfig emulatorConfig; // Added for OrderResponseService

    /**
     * Конструктор с внедрением зависимостей.
     * Инициализирует сервисы и метрики для мониторинга.
     *
     * @param emulatorService - сервис эмулятора
     * @param cpnConfigurationService - сервис конфигурации CPN
     * @param orderResponseService - сервис ответов на запросы заказов
     * @param emulatorConfig - конфигурация эмулятора (для задержек)
     */
    public EmulatorController(EmulatorService emulatorService, 
                              CpnConfigurationService cpnConfigurationService,
                              OrderResponseService orderResponseService,
                              EmulatorConfig emulatorConfig) { 
        this.emulatorService = emulatorService;
        this.cpnConfigurationService = cpnConfigurationService;
        this.orderResponseService = orderResponseService; 
        this.emulatorConfig = emulatorConfig; 
        log.info("Контроллер инициализирован");
    }

    /**
     * Возвращает все items из конфигурации
     *
     * @return Flux<Item> - поток всех items
     */
    @GetMapping("/cpns")
    public Flux<Cpn> getAllCpns() {
        log.debug("Получен запрос на получение всех CPN");
        return cpnConfigurationService.getAllCpns();
    }

    /**
     * Обрабатывает запрос на получение заказов
     *
     * @param request параметры запроса
     * @return Mono<OrderResponse> - реактивный ответ с заказами
     */
    /**
     * Обрабатывает POST запрос на получение заказов
     *
     * @param requestId UUID запроса из заголовка X-Request-Id
     * @param request тело запроса
     * @return Mono<OrderResponse> - реактивный ответ с заказами
     */
    @PostMapping(value = "/cpn/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OrderResponse> getOrders(
            @RequestHeader("X-Request-Id") String requestId,
            @RequestBody OrderRequest request) {
        log.debug("Получен запрос на получение заказов. Request-Id: {}, запрос: {}", requestId, request);
        return orderResponseService.generateOrderResponse(
            requestId,
            request.filters().orderIdFrom(),
            request.filters().orderIds(),
            request.filters().limit()
        );
    }

    /**
     * Обрабатывает GET-запросы к эмулятору.
     * Делегирует обработку в EmulatorService и возвращает реактивный ответ.
     *
     * @return Mono<EmulatorResponse> - реактивный ответ, который будет автоматически преобразован в JSON
     */
    @GetMapping("/emulate")
    public Mono<EmulatorResponse> getEmulatedResponse() {
        return emulatorService.getEmulatedResponse();
    }
}
