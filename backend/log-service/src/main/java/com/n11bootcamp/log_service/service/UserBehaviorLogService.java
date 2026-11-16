package com.n11bootcamp.log_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;

public interface UserBehaviorLogService {

    UserBehaviorLogEntry save(UserBehaviorEvent event);

    List<UserBehaviorRecentItemResponse> findRecentByUserId(UUID userId, int limit);

    Page<UserBehaviorLogEntry> getAll(Pageable pageable);

    Page<UserBehaviorLogEntry> getByUserId(UUID userId, Pageable pageable);

    Page<UserBehaviorLogEntry> getByProductId(UUID productId, Pageable pageable);
}
