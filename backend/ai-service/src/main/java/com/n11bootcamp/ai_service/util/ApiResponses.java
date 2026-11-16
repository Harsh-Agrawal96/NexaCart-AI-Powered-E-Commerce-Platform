package com.n11bootcamp.ai_service.util;

import com.n11bootcamp.common.response.ApiResponse;

/**
 * Feign ile gelen {@link ApiResponse} gövdelerini güvenli açma.
 */
public final class ApiResponses {

    private ApiResponses() {}

    public static <T> T dataOrNull(ApiResponse<T> response) {
        if (response == null || !response.isSuccess()) {
            return null;
        }
        return response.getData();
    }
}
