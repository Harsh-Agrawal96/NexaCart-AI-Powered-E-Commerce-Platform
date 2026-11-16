package com.n11bootcamp.log_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;

public interface UserBehaviorLogEntryRepository extends JpaRepository<UserBehaviorLogEntry, UUID> {

    List<UserBehaviorLogEntry> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);

    Page<UserBehaviorLogEntry> findByUserId(UUID userId, Pageable pageable);

    Page<UserBehaviorLogEntry> findByProductId(UUID productId, Pageable pageable);
}
