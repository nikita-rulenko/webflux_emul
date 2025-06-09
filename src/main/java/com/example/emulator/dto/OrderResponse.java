package com.example.emulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 * DTO для ответа на запрос заказов
 */
@JsonPropertyOrder({ "status", "messages", "data" })
public record OrderResponse(
        @JsonProperty(value = "status", index = 0) String status,
        @JsonProperty(value = "messages", index = 1) List<String> messages,
        @JsonProperty(value = "data", index = 2) OrderResponseData data) {
    @JsonPropertyOrder({ "filters", "stats", "timestamp", "orders" })
    public record OrderResponseData(
            @JsonProperty(value = "filters", index = 0) OrderResponseFilters filters,
            @JsonProperty(value = "stats", index = 1) OrderResponseStats stats,
            @JsonProperty(value = "timestamp", index = 2) String timestamp,
            @JsonProperty(value = "orders", index = 3) List<Order> orders) {
        @JsonPropertyOrder({ "limit", "product_type", "order_id_from", "order_ids" })
        public record OrderResponseFilters(
                @JsonProperty(value = "limit", index = 0) Integer limit,
                @JsonProperty(value = "product_type", index = 1) String productType,
                @JsonProperty(value = "order_id_from", index = 2) Long orderIdFrom,
                @JsonProperty(value = "order_ids", index = 3) List<Long> orderIds) {
        }

        public record OrderResponseStats(
                OrderResponseCouponStats coupon) {
            public record OrderResponseCouponStats(
                    LastOrder lastOrder) {
                public record LastOrder(
                        @JsonProperty("order_id") Long orderId,
                        @JsonProperty("date_created") String dateCreated) {
                }
            }
        }

        @JsonPropertyOrder({
                "order_id", "client_id", "order_id_sbol", "order_number", "order_external_id", "status", "rules",
                "channel", "clientOS", "agreement", "payment_type", "pay_datetime", "promocodes_count",
                "total_amount", "date_created", "product_type", "combined_pdf_url", "reserve_key", "product"
        })
        public record Order(
                @JsonProperty("order_id") Long orderId,
                @JsonProperty("client_id") String clientId,
                @JsonProperty("order_id_sbol") String orderIdSbol,
                @JsonProperty("order_number") Long orderNumber,
                @JsonProperty("order_external_id") String orderExternalId,
                String status,
                String rules,
                String channel,
                String clientOS,
                Boolean agreement,
                @JsonProperty("payment_type") String paymentType,
                @JsonProperty("pay_datetime") String payDatetime,
                @JsonProperty("promocodes_count") Integer promocodesCount,
                @JsonProperty("total_amount") TotalAmount totalAmount,
                @JsonProperty("date_created") String dateCreated,
                @JsonProperty("product_type") String productType,
                @JsonProperty("combined_pdf_url") String combinedPdfUrl,
                @JsonProperty("reserve_key") String reserveKey,
                Product product
        // List<Promocode> promocodes - Удалено, т.к. отсутствует в response_body.json
        // на этом уровне
        ) {
            public record TotalAmount(
                    Integer BON,
                    Integer RUB) {
            }

            @JsonPropertyOrder({ "id", "cpn_id", "conditions", "use", "partner", "offer" })
            public record Product(
                    Long id,
                    @JsonProperty("cpn_id") Long cpnId,
                    String conditions,
                    String use,
                    Partner partner,
                    Offer offer) {
                public record Partner(
                        String id,
                        @JsonProperty("crm_id") String crmId) {
                }

                @JsonPropertyOrder({ "id", "cpn_id", "price", "promocodes" })
                public record Offer(
                        Long id,
                        @JsonProperty("cpn_id") Long cpnId,
                        Integer price,
                        List<Promocode> promocodes) {
                }
            }

            public record Promocode(
                    @JsonProperty("text_code") String textCode,
                    @JsonProperty("qr_code") String qrCode,
                    @JsonProperty("bar_code") String barCode,
                    @JsonProperty("pdf_url") String pdfUrl,
                    Integer type,
                    Integer pin,
                    @JsonProperty("end_date_time") String endDateTime) {
            }
        }
    }
}
