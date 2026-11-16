package com.n11bootcamp.log_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.LogLevel;

public interface SagaLogEntryRepository extends JpaRepository<SagaLogEntry, UUID> {

    Page<SagaLogEntry> findByOrderId(String orderId, Pageable pageable);

    Page<SagaLogEntry> findByUserId(String userId, Pageable pageable);

    Page<SagaLogEntry> findByServiceName(String serviceName, Pageable pageable);

    Page<SagaLogEntry> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<SagaLogEntry> findByLevelAndCreatedAtAfter(LogLevel level, LocalDateTime after);
}
