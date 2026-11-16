package com.n11bootcamp.user_service.service;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.user_service.dto.request.RegisterRequest;
import com.n11bootcamp.user_service.exception.UserErrorCode;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    Keycloak keycloak;

    @Mock
    RealmResource realmResource;

    @Mock
    UsersResource usersResource;

    @Mock
    Response response;

    @InjectMocks
    KeycloakService keycloakService;

    @Test
    @DisplayName("Keycloak: 409 dönerse EMAIL_ALREADY_EXISTS exception fırlatır")
    void createUser_whenKeycloakReturns409_shouldThrowEmailAlreadyExists() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@n11.com")
                .password("password123")
                .build();

        ReflectionTestUtils.setField(keycloakService, "realm", "n11bootcamp");

        given(keycloak.realm("n11bootcamp")).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);
        given(usersResource.create(any())).willReturn(response);
        given(response.getStatus()).willReturn(409);

        // Act + Assert
        assertThatThrownBy(() -> keycloakService.createUser(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS)
                );
    }

    @Test
    @DisplayName("Keycloak: 201 dışı dönerse KEYCLOAK_USER_CREATION_FAILED exception fırlatır")
    void createUser_whenKeycloakReturnsNon201_shouldThrowCreationFailed() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@n11.com")
                .password("password123")
                .build();

        ReflectionTestUtils.setField(keycloakService, "realm", "n11bootcamp");

        given(keycloak.realm("n11bootcamp")).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);
        given(usersResource.create(any())).willReturn(response);
        given(response.getStatus()).willReturn(500);

        // Act + Assert
        assertThatThrownBy(() -> keycloakService.createUser(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.KEYCLOAK_USER_CREATION_FAILED)
                );
    }

    @Test
    @DisplayName("Keycloak: 201 dönerse Location'dan keycloakId parse edip döner")
    void createUser_whenKeycloakReturns201_shouldParseIdFromLocation() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@n11.com")
                .password("password123")
                .build();

        ReflectionTestUtils.setField(keycloakService, "realm", "n11bootcamp");

        given(keycloak.realm("n11bootcamp")).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);
        given(usersResource.create(any())).willReturn(response);
        given(response.getStatus()).willReturn(201);
        given(response.getLocation()).willReturn(URI.create("http://keycloak:8080/admin/realms/n11/users/abc-123"));

        // Act
        String keycloakId = keycloakService.createUser(request);

        // Assert
        assertThat(keycloakId).isEqualTo("abc-123");
    }

    @Test
    @DisplayName("Keycloak: deleteUser realm üzerinden users.delete çağırır")
    void deleteUser_whenCalled_shouldInvokeKeycloakDelete() {
        // Arrange
        ReflectionTestUtils.setField(keycloakService, "realm", "n11bootcamp");

        given(keycloak.realm("n11bootcamp")).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);

        // Act
        keycloakService.deleteUser("kc-1");

        // Assert
        then(usersResource).should().delete("kc-1");
    }
}

