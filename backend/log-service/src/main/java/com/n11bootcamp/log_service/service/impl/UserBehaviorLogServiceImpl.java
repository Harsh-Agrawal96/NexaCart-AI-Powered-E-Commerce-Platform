package com.n11bootcamp.log_service.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.n11bootcamp.common.event.UserBehaviorEvent;
import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.entity.UserBehaviorLogEntry;
import com.n11bootcamp.log_service.mapper.UserBehaviorLogMapper;
import com.n11bootcamp.log_service.repository.UserBehaviorLogEntryRepository;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorLogServiceImpl implements UserBehaviorLogService {

    private static final int MAX_RECENT_LIMIT = 100;

    private final UserBehaviorLogEntryRepository repository;
    private final UserBehaviorLogMapper userBehaviorLogMapper;

    @Override
    public UserBehaviorLogEntry save(UserBehaviorEvent event) {
        UserBehaviorLogEntry entry = userBehaviorLogMapper.toEntity(event);
        UserBehaviorLogEntry saved = repository.save(entry);
        log.info("[LOG-SERVICE] User behavior saved: type={} userId={} productId={}",
                event.getType(), event.getUserId(), event.getProductId());
        return saved;
    }

    @Override
    public List<UserBehaviorRecentItemResponse> findRecentByUserId(UUID userId, int limit) {
        Objects.requireNonNull(userId, "userId");
        int capped = Math.min(Math.max(limit, 1), MAX_RECENT_LIMIT);
        Pageable pageable = PageRequest.of(0, capped);
        List<UserBehaviorLogEntry> rows = repository.findByUserIdOrderByOccurredAtDesc(userId, pageable);
        return userBehaviorLogMapper.toRecentItems(rows);
    }

    @Override
    public Page<UserBehaviorLogEntry> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<UserBehaviorLogEntry> getByUserId(UUID userId, Pageable pageable) {
        Objects.requireNonNull(userId, "userId");
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public Page<UserBehaviorLogEntry> getByProductId(UUID productId, Pageable pageable) {
        Objects.requireNonNull(productId, "productId");
        return repository.findByProductId(productId, pageable);
    }
}
