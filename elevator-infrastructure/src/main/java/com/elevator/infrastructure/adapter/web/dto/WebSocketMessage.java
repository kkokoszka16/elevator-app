package com.elevator.infrastructure.adapter.web.dto;

import java.time.Instant;

public record WebSocketMessage<T>(
        String type,
        Instant timestamp,
        T payload
) {
    public static <T> WebSocketMessage<T> of(String type, T payload) {
        return new WebSocketMessage<>(type, Instant.now(), payload);
    }
}
