package com.n11bootcamp.user_service.service;

import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.user_service.dto.request.CreateUserRequest;
import com.n11bootcamp.user_service.dto.request.RegisterRequest;
import com.n11bootcamp.user_service.dto.request.UpdateUserRequest;
import com.n11bootcamp.user_service.dto.response.UserResponse;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getById(UUID id);
    UserResponse getMe(UUID userId);
    UserResponse getDeletedById(UUID id);
    PageResponse<UserResponse> getAll(Pageable pageable);
    UserResponse getByEmail(String email);
    UserResponse create(CreateUserRequest request);
    UserResponse update(UUID id, UpdateUserRequest request);
    void delete(UUID id);
    UserResponse restore(UUID id);
    UserResponse register(RegisterRequest request);

}