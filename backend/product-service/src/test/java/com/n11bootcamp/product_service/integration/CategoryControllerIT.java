package com.n11bootcamp.product_service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.n11bootcamp.product_service.entity.Category;
import com.n11bootcamp.product_service.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    @MockBean
    ConnectionFactory connectionFactory;

    private UUID activeCategoryId;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        Category active = Category.builder()
                .name("Elektronik")
                .slug("elektronik")
                .active(true)
                .build();
        active = categoryRepository.saveAndFlush(active);
        activeCategoryId = active.getId();

        Category inactive = Category.builder()
                .name("Gizli")
                .slug("gizli")
                .active(false)
                .build();
        categoryRepository.saveAndFlush(inactive);

        Category deleted = Category.builder()
                .name("Silinmiş")
                .slug("silinmis")
                .active(true)
                .build();
        deleted.delete();
        categoryRepository.saveAndFlush(deleted);
    }

    private String testUserId() {
        return UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("GET /api/v1/categories → 200, sadece aktif ve silinmemiş kategoriler gelir")
    void getAllActive_shouldReturnOnlyActiveAndNotDeleted() throws Exception {
        // Act
        String body = mockMvc.perform(get("/api/v1/categories")
                        .header("X-User-Id", testUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        JsonNode data = root.path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.size()).isEqualTo(1);
        assertThat(data.get(0).path("id").asText()).isEqualTo(activeCategoryId.toString());
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} → 200, kategori detayı döner")
    void getById_shouldReturnCategory() throws Exception {
        // Act
        String body = mockMvc.perform(get("/api/v1/categories/{id}", activeCategoryId)
                        .header("X-User-Id", testUserId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        JsonNode root = objectMapper.readTree(body);
        assertThat(root.path("success").asBoolean()).isTrue();
        assertThat(root.path("data").path("id").asText()).isEqualTo(activeCategoryId.toString());
        assertThat(root.path("data").path("name").asText()).isEqualTo("Elektronik");
    }
}

