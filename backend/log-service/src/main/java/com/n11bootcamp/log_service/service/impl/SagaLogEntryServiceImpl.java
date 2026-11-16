package com.n11bootcamp.log_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;
import com.n11bootcamp.log_service.mapper.SagaLogEntryMapper;
import com.n11bootcamp.log_service.repository.SagaLogEntryRepository;
import com.n11bootcamp.log_service.service.SagaLogEntryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SagaLogEntryServiceImpl implements SagaLogEntryService {

    private final SagaLogEntryRepository sagaLogEntryRepository;
    private final SagaLogEntryMapper sagaLogEntryMapper;

    @Override
    @Transactional
    public SagaLogEntry saveSagaEvent(EventType eventType,
                                      String serviceName,
                                      String message,
                                      String orderId,
                                      String userId,
                                      Object payload) {

        SagaLogEntry entry = sagaLogEntryMapper.toSagaEventEntry(
                eventType, serviceName, message, orderId, userId, payload);

        SagaLogEntry saved = sagaLogEntryRepository.save(entry);
        log.info("[LOG-SERVICE] Saga event saved: {} | orderId={} | userId={}",
                eventType, orderId, userId);
        return saved;
    }

    @Override
    @Transactional
    public SagaLogEntry saveApplicationLog(String serviceName,
                                           LogLevel level,
                                           String message,
                                           String orderId,
                                           String userId) {

        SagaLogEntry entry = sagaLogEntryMapper.toApplicationLogEntry(
                serviceName, level, message, orderId, userId);

        SagaLogEntry saved = sagaLogEntryRepository.save(entry);
        log.info("[LOG-SERVICE] App log saved: level={} service={}", level, serviceName);
        return saved;
    }

    @Override
    public Page<SagaLogEntry> getAllLogs(Pageable pageable) {
        return sagaLogEntryRepository.findAll(pageable);
    }

    @Override
    public Page<SagaLogEntry> getLogsByOrderId(String orderId, Pageable pageable) {
        return sagaLogEntryRepository.findByOrderId(orderId, pageable);
    }

    @Override
    public Page<SagaLogEntry> getLogsByUserId(String userId, Pageable pageable) {
        return sagaLogEntryRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<SagaLogEntry> getLogsByServiceName(String serviceName, Pageable pageable) {
        return sagaLogEntryRepository.findByServiceName(serviceName, pageable);
    }
}
