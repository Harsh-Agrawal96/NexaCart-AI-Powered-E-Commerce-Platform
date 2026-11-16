package com.n11bootcamp.ai_service.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.n11bootcamp.ai_service.config.FeignConfig;
import com.n11bootcamp.ai_service.dto.UserBehaviorRecentItemDto;

@FeignClient(
        name = "logServiceClient",
        url = "${services.log.url}",
        configuration = FeignConfig.class
)
public interface LogServiceClient {

    @GetMapping("/internal/user-behavior/{userId}/recent")
    List<UserBehaviorRecentItemDto> getRecentBehaviors(
            @PathVariable("userId") UUID userId,
            @RequestParam("limit") int limit);
}
