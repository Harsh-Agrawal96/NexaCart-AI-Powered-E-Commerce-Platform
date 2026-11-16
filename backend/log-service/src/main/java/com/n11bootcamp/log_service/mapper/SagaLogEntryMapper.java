package com.n11bootcamp.log_service.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class SagaLogEntryMapper {

    private final ObjectMapper objectMapper;

    public SagaLogEntry toSagaEventEntry(EventType eventType,
                                         String serviceName,
                                         String message,
                                         String orderId,
                                         String userId,
                                         Object payload) {
        return SagaLogEntry.builder()
                .eventType(eventType)
                .serviceName(serviceName)
                .level(LogLevel.INFO)
                .message(message)
                .orderId(orderId)
                .userId(userId)
                .payload(toPayloadJson(payload))
                .build();
    }

    public SagaLogEntry toApplicationLogEntry(String serviceName,
                                              LogLevel level,
                                              String message,
                                              String orderId,
                                              String userId) {
        return SagaLogEntry.builder()
                .eventType(EventType.APPLICATION_LOG)
                .serviceName(serviceName)
                .level(level)
                .message(message)
                .orderId(orderId)
                .userId(userId)
                .payload(null)
                .build();
    }

    public String toPayloadJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("[SagaLogEntryMapper] Payload serialize edilemedi: {}", e.getMessage());
            return payload.toString();
        }
    }
}
