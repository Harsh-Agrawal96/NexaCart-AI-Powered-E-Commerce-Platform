package com.n11bootcamp.user_service.service;

import com.n11bootcamp.user_service.dto.request.RegisterRequest;
import com.n11bootcamp.user_service.exception.UserErrorCode;
import com.n11bootcamp.common.exception.BusinessException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createUser(RegisterRequest request) {
        UserRepresentation user = buildUserRepresentation(request);

        Response response = keycloak.realm(realm).users().create(user);

        if (response.getStatus() == 409) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (response.getStatus() != 201) {
            log.error("Keycloak user creation failed. Status: {}", response.getStatus());
            throw new BusinessException(UserErrorCode.KEYCLOAK_USER_CREATION_FAILED);
        }

        // Location header'dan keycloakId çek
        // http://keycloak:8080/admin/realms/n11/users/{id}
        String location = response.getLocation().toString();
        String keycloakId = location.substring(location.lastIndexOf("/") + 1);

        log.info("Keycloak user created: {}", keycloakId);
        return keycloakId;
    }

    public void deleteUser(String keycloakId) {
        keycloak.realm(realm).users().delete(keycloakId);
        log.info("Keycloak user deleted: {}", keycloakId);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private UserRepresentation buildUserRepresentation(RegisterRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(List.of(credential));

        return user;
    }
}
