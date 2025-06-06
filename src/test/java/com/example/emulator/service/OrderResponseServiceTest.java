package com.example.emulator.service;

import com.example.emulator.config.EmulatorConfig;
import com.example.emulator.dto.Cpn;
import com.example.emulator.dto.OrderResponse;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderResponseServiceTest {

    @Mock
    private CpnConfigurationService cpnConfigurationService;

    @Mock
    private EmulatorConfig emulatorConfig;
    @Mock
    private EmulatorConfig.Delay delayConfig;

    private OrderResponseService orderResponseService;

    private ObjectMapper objectMapper;

    private final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 6, 5, 12, 0, 0);
    private final String FIXED_UUID_STRING = "220e8550-e29b-4174-a7b6-11115440000"; // Example from response_body
    private final UUID FIXED_UUID = UUID.fromString(FIXED_UUID_STRING);

    private final DateTimeFormatter DATE_TIME_FORMATTER_NO_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Europe/Moscow"));
    private final DateTimeFormatter DATE_TIME_FORMATTER_WITH_ZONE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX").withZone(ZoneId.of("Europe/Moscow"));

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // objectMapper.getFactory().configure(com.fasterxml.jackson.core.json.JsonWriteFeature.ESCAPE_FORWARD_SLASHES.mappedFeature(), false); // Temporarily commented out due to resolution issues
        
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF); // 4 spaces
        prettyPrinter.indentObjectsWith(indenter);
        prettyPrinter.indentArraysWith(indenter);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);

        when(emulatorConfig.getDelay()).thenReturn(delayConfig);
        orderResponseService = new OrderResponseService(cpnConfigurationService, emulatorConfig);
    }

    @Test
    void generateOrderResponse_shouldProduceCorrectJsonStructure() throws Exception {
        // 1. Setup mocks and input data
        Cpn.CpnOffer testOffer = new Cpn.CpnOffer(123456789012345L, "123456789", 10); // id, omniId, price // omniId, id
        Cpn testCpn = new Cpn(
                123456789012345L,      // id
                "123003789",           // omniId
                "test use",            // use
                "test condition",      // condition
                201L,                  // partnerOmniId (was String "201", now long)
                "partner-crm-id-value",// partnerCrmId
                List.of(testOffer)
        );
        when(cpnConfigurationService.getAllCpns()).thenReturn(Flux.just(testCpn));
        // Delays don't matter for content test, but config is needed
        when(delayConfig.getMin()).thenReturn(10); 
        when(delayConfig.getMax()).thenReturn(20);

        // Mock static methods for controlling time and UUID
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<UUID> mockedUuid = Mockito.mockStatic(UUID.class, Mockito.CALLS_REAL_METHODS)) {

            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(FIXED_NOW);
            mockedUuid.when(UUID::randomUUID).thenReturn(FIXED_UUID);

            String requestId = "test-req-json-structure";
            Long orderIdFrom = 1L;
            Integer limit = 1; // For simplicity of expected JSON, generate 1 order

            // 2. Call the method under test
            Mono<OrderResponse> actualResponseMono = orderResponseService.generateOrderResponse(requestId, orderIdFrom, null, limit);
            OrderResponse actualOrderResponse = actualResponseMono.block(Duration.ofSeconds(5)); // Block for test

            // 3. Construct the expected OrderResponse object
            // Using FIXED_NOW, FIXED_UUID, and service logic
            String formattedFixedNowWithZone = FIXED_NOW.format(DATE_TIME_FORMATTER_WITH_ZONE);
            String formattedFixedNowNoZone = FIXED_NOW.format(DATE_TIME_FORMATTER_NO_ZONE);
            // In service, promocode end_date_time is now() + 1 hour, not +1 year as in response_body.json
            String formattedPromocodeEndDate = FIXED_NOW.plusHours(1).format(DATE_TIME_FORMATTER_NO_ZONE); 

            OrderResponse.OrderResponseData.Order.Promocode expectedPromocode = new OrderResponse.OrderResponseData.Order.Promocode(
                    "CODE123", null, null, null, 0, 1234, formattedPromocodeEndDate
            );
            OrderResponse.OrderResponseData.Order.Product.Offer expectedProductOffer = new OrderResponse.OrderResponseData.Order.Product.Offer(
                    Long.parseLong(testOffer.omniId()), testOffer.id(), testOffer.price(), List.of(expectedPromocode)
            );
            OrderResponse.OrderResponseData.Order.Product.Partner expectedPartner = new OrderResponse.OrderResponseData.Order.Product.Partner(
                    String.valueOf(testCpn.partnerOmniId()), testCpn.partnerCrmId()
            );
            OrderResponse.OrderResponseData.Order.Product expectedProduct = new OrderResponse.OrderResponseData.Order.Product(
                    Long.parseLong(testCpn.omniId()), testCpn.id(), "conditions", "use", expectedPartner, expectedProductOffer
            );
            // client_id is generated by a static Random, its value will be unpredictable in test without more complex setup
            // order_number is orderIdFrom + index (0-based for loop in service)
            OrderResponse.OrderResponseData.Order expectedOrder = new OrderResponse.OrderResponseData.Order(
                    "ANY_CLIENT_ID", // Placeholder, will be handled by custom comparator
                    null, // order_id_sbol
                    orderIdFrom + 0, // order_number
                    null, // order_external_id
                    "success", // status
                    "https://rules.pdf", // rules
                    "web", // channel
                    null, // clientOS
                    true, // agreement
                    "spsBonuses", // payment_type
                    formattedFixedNowWithZone, // pay_datetime (now() in service)
                    1, // promocodes_count
                    new OrderResponse.OrderResponseData.Order.TotalAmount(null, 100), // total_amount
                    formattedFixedNowWithZone, // date_created (now() in service)
                    "coupon", // product_type
                    "https://combined.pdf", // combined_pdf_url
                    FIXED_UUID_STRING, // reserve_key (mocked UUID)
                    expectedProduct
            );

            OrderResponse.OrderResponseData.OrderResponseFilters expectedFilters = new OrderResponse.OrderResponseData.OrderResponseFilters(
                    limit, "coupon", orderIdFrom, null
            );
            // In service, lastOrder.order_id is orderIdFrom + 10
            OrderResponse.OrderResponseData.OrderResponseStats.OrderResponseCouponStats.LastOrder expectedLastOrder =
                    new OrderResponse.OrderResponseData.OrderResponseStats.OrderResponseCouponStats.LastOrder(
                            orderIdFrom + 10, formattedFixedNowNoZone // date_created (now() in service)
                    );
            OrderResponse.OrderResponseData.OrderResponseStats expectedStats = new OrderResponse.OrderResponseData.OrderResponseStats(
                    new OrderResponse.OrderResponseData.OrderResponseStats.OrderResponseCouponStats(expectedLastOrder)
            );
            OrderResponse.OrderResponseData expectedData = new OrderResponse.OrderResponseData(
                    expectedFilters, expectedStats, formattedFixedNowWithZone, List.of(expectedOrder)
            );
            OrderResponse expectedResponse = new OrderResponse("success", List.of(), expectedData);

            // 4. Serialize to JSON and assert
            String actualJson = objectMapper.writeValueAsString(actualOrderResponse);
            String expectedJson = objectMapper.writeValueAsString(expectedResponse);
            
            // For debugging if needed:
            // System.out.println("Actual JSON:\n" + actualJson);
            // System.out.println("Expected JSON (with placeholder client_id):\n" + expectedJson);

            JSONAssert.assertEquals(expectedJson, actualJson,
                    new CustomComparator(JSONCompareMode.STRICT,
                            // client_id is random, so we only check if it's a string of digits
                            new Customization("data.orders[0].client_id", (o1, o2) -> (o2 instanceof String && ((String)o2).matches("\\d+")))
                    )
            );
        }
    }

    @Test
    void generateOrderResponse_shouldReturnResponseWithinConfiguredDelay() {
        int minDelayMillis = 50;
        int maxDelayMillis = 150;
        when(delayConfig.getMin()).thenReturn(minDelayMillis);
        when(delayConfig.getMax()).thenReturn(maxDelayMillis);

        // Need a CPN for the service to proceed
        Cpn.CpnOffer testOffer = new Cpn.CpnOffer(999L, "delayTestOfferOmniId", 20); // id, omniId, price
        Cpn testCpn = new Cpn(999L, "delayTestCpnOmniId", "delay use", "delay condition", 202L, "crm-delay-id", List.of(testOffer)); // Added use, condition, partnerOmniId as long
        when(cpnConfigurationService.getAllCpns()).thenReturn(Flux.just(testCpn));

        // We don't mock time/UUID here as we only care about emission timing
        Mono<OrderResponse> responseMono = orderResponseService.generateOrderResponse("test-req-delay", 1L, null, 1);

        StepVerifier.<OrderResponse>withVirtualTime(() -> responseMono)
                .expectSubscription()
                .thenAwait(Duration.ofMillis(minDelayMillis -1 )) // Wait until just before minDelay
                .expectNoEvent(Duration.ofMillis(1)) // Confirm no event fires exactly at minDelay-1 or during this 1ms check
                // The item should be emitted between minDelay and maxDelay (inclusive)
                // So, after minDelay has passed, we expect the item. Then we wait for remaining (maxDelay - minDelay)
                .thenAwait(Duration.ofMillis(1)) // Advance time to minDelay
                .expectNextCount(1) // Expect the item to be emitted now or very soon
                .thenAwait(Duration.ofMillis(maxDelayMillis - minDelayMillis)) // Wait for the remainder of the max window
                .verifyComplete();
    }
}
