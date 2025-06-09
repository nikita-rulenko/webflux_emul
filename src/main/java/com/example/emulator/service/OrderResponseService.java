package com.example.emulator.service;

import com.example.emulator.config.EmulatorConfig;
import com.example.emulator.dto.Cpn;
import com.example.emulator.dto.OrderResponse;
import com.example.emulator.dto.OrderResponse.OrderResponseData;
import com.example.emulator.dto.OrderResponse.OrderResponseData.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сервис для генерации ответов на запросы заказов.
 * Использует CpnConfigurationService для получения данных о купонах
 * и генерирует динамические ответы на основе параметров запроса.
 */
@Service
public class OrderResponseService {
    private static final String STATUS_SUCCESS = "success";
    private static final String PRODUCT_TYPE_COUPON = "coupon";
    private static final String RULES_URL = "https://rules.pdf";
    private static final String CHANNEL_WEB = "web";
    private static final String PAYMENT_TYPE_SPS_BONUSES = "spsBonuses";
    private static final String COMBINED_PDF_URL = "https://combined.pdf";
    private static final String CONDITIONS_TEXT = "conditions";
    private static final String USE_TEXT = "use";
    private static final String PROMO_CODE_TEXT = "CODE123";
    private static final Logger log = LoggerFactory.getLogger(OrderResponseService.class);
    /** Целевой формат даты и времени */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final Random randomGenerator = new Random(); // Renamed to avoid conflict with new field

    private final CpnConfigurationService cpnConfigurationService;
    private final Random random; // For delay
    private final int minDelay;
    private final int maxDelay;
    private final int delayRange;

    public OrderResponseService(CpnConfigurationService cpnConfigurationService, EmulatorConfig emulatorConfig) {
        this.cpnConfigurationService = cpnConfigurationService;
        this.random = new Random();
        this.minDelay = emulatorConfig.getDelay().getMin();
        this.maxDelay = emulatorConfig.getDelay().getMax();
        this.delayRange = this.maxDelay - this.minDelay + 1;
        log.info("OrderResponseService initialized with delay {} - {} ms", this.minDelay, this.maxDelay);
    }

    /**
     * Генерирует ответ на запрос заказов.
     * 
     * @param requestId Идентификатор запроса
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @return Mono с объектом ответа, содержащим список заказов и метаданные
     */
    public Mono<OrderResponse> generateOrderResponse(String requestId, Long orderIdFrom, List<Long> orderIds, Integer limit) {
        log.info("Generating response for request: {}", requestId);
        return cpnConfigurationService.getAllCpns()
                .collectList()
                .map(cpns -> {
                    log.info("Got {} cpns from configuration", cpns.size());
                    int index = random.nextInt(cpns.size());
                    Cpn selectedCpn = cpns.get(index);
                    log.info("Selected cpn with id: {}", selectedCpn.id());
                    return selectedCpn;
                })
                .map(cpn -> createOrderResponse(orderIdFrom, orderIds, limit, cpn))
                .delayElement(getRandomDelay()); // Apply random delay
    }

    /**
     * Создает объект ответа на основе параметров запроса и выбранного купона.
     * 
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @param cpn Выбранный купон для генерации заказов
     * @return Объект ответа с заказами и метаданными
     */
    private OrderResponse createOrderResponse(Long orderIdFrom, List<Long> orderIds, Integer limit, Cpn cpn) {
        log.info("Creating order response with cpn: {}", cpn);
        LocalDateTime now = LocalDateTime.now();
        // LocalDateTime hourAgo = now.minusHours(1); // Больше не используется для времени создания заказа

        try {
            // Передаем 'now' для времени создания заказа, чтобы соответствовать требованию
            List<OrderResponseData.Order> orders = createOrders(orderIdFrom, orderIds, limit, cpn, now);
            log.info("Created {} orders", orders.size());
            
            var timestamp = formatDateTime(now);
            var response = createOrderResponse(orders, orderIdFrom, orderIds, limit, timestamp);
            
            return response;
        } catch (Exception e) {
            log.error("Error creating order response: {}", e.getMessage(), e);
            throw e;
        }
    }

    private OrderResponse createOrderResponse(List<Order> orders, Long orderIdFrom, List<Long> orderIds, Integer limit, String timestamp) {
        var filters = createFilters(orderIdFrom, orderIds, limit);
        var stats = createStats(orderIdFrom);
        
        return new OrderResponse(
                STATUS_SUCCESS,
                List.of(),
                new OrderResponseData(
                        filters,
                        stats,
                        timestamp,
                        orders
                )
        );
    }

    /**
     * Создает список заказов на основе параметров запроса.
     * Количество заказов определяется либо параметром limit, либо количеством orderIds.
     * 
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @param cpn Купон для создания заказов
     * @param orderTime Время создания заказов
     * @return Список сгенерированных заказов
     */
    private List<OrderResponseData.Order> createOrders(Long orderIdFrom, List<Long> orderIds, Integer limit, Cpn cpn, LocalDateTime orderTime) {
        List<OrderResponseData.Order> orders = new ArrayList<>();
        int orderCount = determineOrderCount(orderIds, limit);
        
        for (int i = 0; i < orderCount; i++) {
            orders.add(createOrder(orderIdFrom, orderIds, cpn, orderTime, i));
        }
        
        return orders;
    }

    /**
     * Определяет количество заказов для генерации.
     * Если указаны orderIds, возвращает их количество.
     * Иначе возвращает значение limit или 1 по умолчанию.
     * 
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @return Количество заказов для генерации
     */
    private int determineOrderCount(List<Long> orderIds, Integer limit) {
        if (orderIds != null && !orderIds.isEmpty()) {
            return orderIds.size();
        }
        return limit != null ? limit : 1;
    }

    /**
     * Создает один заказ с уникальными параметрами.
     * 
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @param cpn Купон для создания заказа
     * @param orderTime Время создания заказа
     * @param index Индекс заказа в списке (для генерации orderNumber)
     * @return Объект заказа
     */
    private OrderResponseData.Order createOrder(Long orderIdFrom, List<Long> orderIds, Cpn cpn, LocalDateTime orderTime, int index) {
        if (cpn.offers() == null || cpn.offers().isEmpty()) {
            throw new IllegalStateException("No offers found in CPN");
        }
        var selectedOffer = cpn.offers().get(0); // Берем первый оффер для примера
        
        return new OrderResponseData.Order(
            String.valueOf(randomGenerator.nextInt(1000000)), // client_id - случайное число строкой
            null,  // order_id_sbol
            determineOrderNumber(orderIdFrom, orderIds, index), // order_number
            null,  // order_external_id
            STATUS_SUCCESS, // status
            RULES_URL, // rules
            CHANNEL_WEB, // channel
            null,  // clientOS
            true,  // agreement
            PAYMENT_TYPE_SPS_BONUSES, // payment_type
            formatDateTime(orderTime), // pay_datetime
            1,    // promocodes_count
            new OrderResponseData.Order.TotalAmount(null, 100), // total_amount
            formatDateTime(orderTime), // date_created
            PRODUCT_TYPE_COUPON, // product_type
            COMBINED_PDF_URL, // combined_pdf_url
            UUID.randomUUID().toString(), // reserve_key
            createProduct(cpn, selectedOffer)
        );
    }

    /**
     * Определяет номер заказа.
     * Если указаны orderIds, берет значение из списка.
     * Иначе генерирует последовательно от orderIdFrom.
     * 
     * @param orderIdFrom Начальный идентификатор заказа
     * @param orderIds Список идентификаторов заказов
     * @param limit Количество заказов для генерации
     * @param index Индекс заказа в списке
     * @return Номер заказа
     */
    private Long determineOrderNumber(Long orderIdFrom, List<Long> orderIds, int index) {
        if (orderIds != null && !orderIds.isEmpty()) {
            return orderIds.get(index);
        }
        return orderIdFrom != null ? orderIdFrom + index : (long) (index + 1);
    }

    /**
     * Создает информацию о продукте на основе купона и оффера.
     * 
     * @param cpn Купон с основной информацией
     * @param offer Оффер с дополнительной информацией
     * @return Объект продукта
     */
    private OrderResponseData.Order.Product createProduct(Cpn cpn, Cpn.CpnOffer offer) {
        return new OrderResponseData.Order.Product(
            Long.parseLong(cpn.omniId()),
            cpn.id(),
            CONDITIONS_TEXT, // conditions - Static value from response_body.json
            USE_TEXT, // use - Static value from response_body.json
            new OrderResponseData.Order.Product.Partner(
                String.valueOf(cpn.partnerOmniId()), // id оставляем String
                cpn.partnerCrmId() // crmId
            ),
            new OrderResponseData.Order.Product.Offer(
                Long.parseLong(offer.omniId()),
                offer.id(), // cpnId
                10, // price
                List.of( // Create a list of promocodes for the Offer
                    new OrderResponseData.Order.Promocode(
                        PROMO_CODE_TEXT, // text_code - Static value from response_body.json
                        null,       // qr_code
                        null,       // bar_code
                        null, // pdf_url - Static value (null) from response_body.json
                        0,          // type
                        1234,      // pin - СТАЛО 1234
                        formatDateTime(LocalDateTime.now().minusDays(1)) // end_date_time
                    )
                )
            )
        ); // End of Product constructor and return statement
    } // End of createProduct method

    /**
     * Создает статистику по заказам.
     * Включает информацию о последнем заказе.
     *
     * @param orderIdFrom Начальный идентификатор заказа
     * @return Объект статистики
     */
    private OrderResponseData.OrderResponseStats createStats(Long orderIdFrom) {
        Long lastOrderIdInStats = null;
        if (orderIdFrom != null) {
            lastOrderIdInStats = orderIdFrom + 10;
        }
        String dateCreatedInStats = formatDateTime(LocalDateTime.now());

        // Assuming OrderResponseCouponStats and LastOrder are nested correctly as per DTO structure
        // This part might need adjustment if DTO structure for Stats is different
        return new OrderResponseData.OrderResponseStats(
            new OrderResponseData.OrderResponseStats.OrderResponseCouponStats( // Corrected nested class based on DTO
                new OrderResponseData.OrderResponseStats.OrderResponseCouponStats.LastOrder( // Corrected nested class based on DTO
                    lastOrderIdInStats,
                    dateCreatedInStats
                )
            )
        );
    }

    private OrderResponseData.OrderResponseFilters createFilters(Long orderIdFromParam, List<Long> orderIdsParam, Integer limitParam) {
        Long responseOrderIdFrom = orderIdFromParam;
        List<Long> responseOrderIds = orderIdsParam;

        if (orderIdsParam != null && !orderIdsParam.isEmpty()) {
            responseOrderIdFrom = null; // Если есть order_ids, то order_id_from в ответе null
        } else if (orderIdFromParam != null) {
            responseOrderIds = null; // Если есть order_id_from, то order_ids в ответе null
        }

        return new OrderResponseData.OrderResponseFilters(
            limitParam,
            PRODUCT_TYPE_COUPON,
            responseOrderIdFrom,
            responseOrderIds
        );
    }



/**
 * Возвращает случайную задержку на основе конфигурации эмулятора.
 * 
 * @return Случайная задержка
 */
private Duration getRandomDelay() {
    if (delayRange <= 0) { // Should not happen if config is correct (minDelay <= maxDelay)
        return Duration.ofMillis(minDelay);
    }
    return Duration.ofMillis(random.nextInt(delayRange) + minDelay);
}

/**
 * Форматирует LocalDateTime в строку согласно DATE_TIME_FORMATTER с фиксированным смещением +03:00.
 *
 * @param localDateTime Дата и время для форматирования (без зоны)
 * @return Отформатированная строка с датой и временем и смещением +03:00
 */
private String formatDateTime(LocalDateTime localDateTime) {
    if (localDateTime == null) {
        return null;
    }
    // Применяем фиксированное смещение +03:00, так как XXX в паттерне требует информации о зоне/смещении
    return localDateTime.atOffset(ZoneOffset.ofHours(3)).format(DATE_TIME_FORMATTER);
}
}
