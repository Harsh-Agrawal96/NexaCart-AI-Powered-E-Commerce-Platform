package com.n11bootcamp.common.response;
import java.time.OffsetDateTime;

import org.springframework.http.ProblemDetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final OffsetDateTime timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .success(true)
                .timestamp(OffsetDateTime.now())
                .build();
    }
    
    public static ApiResponse<ProblemDetail> error(ProblemDetail problem) {
        return ApiResponse.<ProblemDetail>builder()
                .success(false)
                .data(problem)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}