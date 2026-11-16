package com.n11bootcamp.log_service.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.n11bootcamp.common.enums.UserBehaviorType;
import com.n11bootcamp.log_service.dto.response.UserBehaviorRecentItemResponse;
import com.n11bootcamp.log_service.service.UserBehaviorLogService;

@WebMvcTest(controllers = UserBehaviorInternalController.class)
@ActiveProfiles("test")
class UserBehaviorInternalControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserBehaviorLogService userBehaviorLogService;

    @Test
    @DisplayName("GET /internal/user-behavior/{userId}/recent → 200, liste")
    void getRecent_shouldReturn200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var item = new UserBehaviorRecentItemResponse(
                productId,
                UserBehaviorType.PRODUCT_VIEWED,
                Instant.parse("2026-05-02T12:00:00Z"),
                "{}");
        given(userBehaviorLogService.findRecentByUserId(userId, 10)).willReturn(List.of(item));

        mockMvc.perform(get("/internal/user-behavior/{userId}/recent", userId)
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$[0].behaviorType").value("PRODUCT_VIEWED"));
    }
}
