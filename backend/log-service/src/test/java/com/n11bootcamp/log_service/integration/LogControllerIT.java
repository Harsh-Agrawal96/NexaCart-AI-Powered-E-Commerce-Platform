package com.n11bootcamp.log_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.log_service.controller.LogController;
import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;
import com.n11bootcamp.log_service.service.SagaLogEntryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {com.n11bootcamp.log_service.LogServiceApplication.class, LogController.class},
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false",
                "spring.config.import=optional:file:./.test-config.yml"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LogControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SagaLogEntryService sagaLogEntryService;

    @Test
    @DisplayName("GET /api/v1/logs → 200, page yapısı döner")
    void getAllLogs_shouldReturn200() throws Exception {
        SagaLogEntry entry = SagaLogEntry.builder()
                .eventType(EventType.APPLICATION_LOG)
                .serviceName("order-service")
                .level(LogLevel.INFO)
                .message("hello")
                .orderId("order-1")
                .userId("user-1")
                .payload(null)
                .build();

        Page<SagaLogEntry> page = new PageImpl<>(List.of(entry));
        given(sagaLogEntryService.getAllLogs(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/v1/logs")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].serviceName").value("order-service"))
                .andExpect(jsonPath("$.content[0].level").value("INFO"));
    }
}

