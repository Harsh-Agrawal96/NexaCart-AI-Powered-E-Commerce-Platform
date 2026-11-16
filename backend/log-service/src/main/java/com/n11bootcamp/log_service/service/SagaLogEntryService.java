package com.n11bootcamp.log_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;

public interface SagaLogEntryService {

    SagaLogEntry saveSagaEvent(EventType eventType,
                               String serviceName,
                               String message,
                               String orderId,
                               String userId,
                               Object payload);

    SagaLogEntry saveApplicationLog(String serviceName,
                                    LogLevel level,
                                    String message,
                                    String orderId,
                                    String userId);

    Page<SagaLogEntry> getAllLogs(Pageable pageable);

    Page<SagaLogEntry> getLogsByOrderId(String orderId, Pageable pageable);

    Page<SagaLogEntry> getLogsByUserId(String userId, Pageable pageable);

    Page<SagaLogEntry> getLogsByServiceName(String serviceName, Pageable pageable);
}
