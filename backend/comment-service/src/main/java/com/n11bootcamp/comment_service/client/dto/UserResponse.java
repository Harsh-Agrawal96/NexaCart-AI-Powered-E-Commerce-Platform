package com.n11bootcamp.comment_service.client.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {}

