package com.n11bootcamp.log_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SagaLogEntryMapperTest {

    @Mock ObjectMapper objectMapper;

    @InjectMocks SagaLogEntryMapper sagaLogEntryMapper;

    @Test
    @DisplayName("Payload serialize edilemezse: toString fallback")
    void payloadToJson_whenJacksonFails_usesToString() throws Exception {
        Object payload = new Payload("x", 1);
        given(objectMapper.writeValueAsString(payload)).willThrow(new JsonProcessingException("boom") {});

        assertThat(sagaLogEntryMapper.toPayloadJson(payload)).isEqualTo(payload.toString());
    }

    private record Payload(String x, int n) {}
}
