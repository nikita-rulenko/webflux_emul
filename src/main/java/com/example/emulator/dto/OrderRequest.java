package com.example.emulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO для входящего запроса на получение заказов
 */
public record OrderRequest(
    Filters filters,
    Stats stats
) {
    public record Filters(
        Integer limit,
        @JsonProperty("product_type")
        String productType,
        @JsonProperty("order_ids")
        List<Long> orderIds,
        @JsonProperty("order_id_from")
        Long orderIdFrom
    ) {}

    public record Stats(
        Integer coupon
    ) {}
}
