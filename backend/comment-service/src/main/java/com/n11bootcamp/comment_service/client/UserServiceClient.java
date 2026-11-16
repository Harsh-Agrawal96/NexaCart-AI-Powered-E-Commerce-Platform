package com.n11bootcamp.comment_service.client;

import com.n11bootcamp.comment_service.client.dto.UserResponse;
import com.n11bootcamp.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${services.user.url}", path = "/internal/users")
public interface UserServiceClient {

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getById(@PathVariable UUID id);
}

