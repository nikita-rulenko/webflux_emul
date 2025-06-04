package com.example.emulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO для ответа эмулятора.
 * Представляет собой иммутабельный объект с тремя полями:
 * - timestamp: время в миллисекундах
 * - status: статус ответа
 * - message: текстовое сообщение
 *
 * Использует Jackson для сериализации в JSON.
 */
public class EmulatorResponse {
    @JsonProperty("timestamp")
    private final long timestamp;
    
    @JsonProperty("status")
    private final String status;
    
    @JsonProperty("message")
    private final String message;

    private EmulatorResponse(long timestamp, String status, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
    }

    /**
     * Создает новый ответ с текущим временем.
     *
     * @return EmulatorResponse с заполненными полями
     */
    public static EmulatorResponse createResponse() {
        return new EmulatorResponse(
            System.currentTimeMillis(),
            "success",
            "Response generated at: " + LocalDateTime.now()
        );
    }
}
