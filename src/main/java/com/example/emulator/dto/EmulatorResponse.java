package com.example.emulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO для ответа эмулятора.
 * Инкапсулирует структуру JSON-ответа.
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
