package com.n11bootcamp.log_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.log_service.entity.SagaLogEntry;
import com.n11bootcamp.log_service.entity.enums.EventType;
import com.n11bootcamp.log_service.entity.enums.LogLevel;
import com.n11bootcamp.log_service.mapper.SagaLogEntryMapper;
import com.n11bootcamp.log_service.repository.SagaLogEntryRepository;
import com.n11bootcamp.log_service.service.impl.SagaLogEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SagaLogEntryServiceImplTest {

    @Mock SagaLogEntryRepository sagaLogEntryRepository;

    SagaLogEntryServiceImpl sagaLogEntryService;

    @BeforeEach
    void setUp() {
        sagaLogEntryService = new SagaLogEntryServiceImpl(
                sagaLogEntryRepository,
                new SagaLogEntryMapper(new ObjectMapper()));
    }

    @Test
    @DisplayName("Saga event kaydı: mapper üzerinden payload JSON ve INFO level")
    void saveSagaEvent_shouldSerializePayloadAndSave() {
        Object payload = new Payload("abc", 42);
        given(sagaLogEntryRepository.save(org.mockito.ArgumentMatchers.any(SagaLogEntry.class)))
                .willAnswer(inv -> inv.getArgument(0));

        SagaLogEntry saved = sagaLogEntryService.saveSagaEvent(
                EventType.ORDER_CREATED,
                "order-service",
                "Order created",
                "order-1",
                "user-1",
                payload
        );

        assertThat(saved).isNotNull();
        assertThat(saved.getEventType()).isEqualTo(EventType.ORDER_CREATED);
        assertThat(saved.getServiceName()).isEqualTo("order-service");
        assertThat(saved.getLevel()).isEqualTo(LogLevel.INFO);
        assertThat(saved.getPayload()).contains("abc").contains("42");

        ArgumentCaptor<SagaLogEntry> captor = ArgumentCaptor.forClass(SagaLogEntry.class);
        then(sagaLogEntryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPayload()).isEqualTo(saved.getPayload());
    }

    @Test
    @DisplayName("Application log kaydı: APPLICATION_LOG eventType ve payload=null")
    void saveApplicationLog_shouldSaveWithApplicationLogType() {
        given(sagaLogEntryRepository.save(org.mockito.ArgumentMatchers.any(SagaLogEntry.class)))
                .willAnswer(inv -> inv.getArgument(0));

        SagaLogEntry saved = sagaLogEntryService.saveApplicationLog(
                "cart-service",
                LogLevel.ERROR,
                "Something went wrong",
                "order-3",
                "user-3"
        );

        assertThat(saved.getEventType()).isEqualTo(EventType.APPLICATION_LOG);
        assertThat(saved.getLevel()).isEqualTo(LogLevel.ERROR);
        assertThat(saved.getPayload()).isNull();
    }

    private record Payload(String name, int count) {}
}
