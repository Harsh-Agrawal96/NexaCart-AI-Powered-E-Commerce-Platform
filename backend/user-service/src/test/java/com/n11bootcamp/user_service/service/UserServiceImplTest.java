package com.n11bootcamp.user_service.service;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.exception.ResourceNotFoundException;
import com.n11bootcamp.user_service.dto.request.CreateUserRequest;
import com.n11bootcamp.user_service.dto.request.RegisterRequest;
import com.n11bootcamp.user_service.dto.response.UserResponse;
import com.n11bootcamp.user_service.entity.User;
import com.n11bootcamp.user_service.exception.UserErrorCode;
import com.n11bootcamp.user_service.mapper.UserMapper;
import com.n11bootcamp.user_service.repository.UserRepository;
import com.n11bootcamp.user_service.service.impl.UserServiceImpl;
import com.n11bootcamp.user_service.validator.UserValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserValidator userValidator;

    @Mock
    UserMapper userMapper;

    @Mock
    KeycloakService keycloakService;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    @DisplayName("Register: email uygun ise Keycloak + DB kaydı yapılır ve response döner")
    void register_whenEmailAvailable_shouldCreateKeycloakUserAndPersistUser() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@n11.com")
                .password("password123")
                .phoneNumber("+905551112233")
                .build();

        String keycloakId = "kc-123";
        UUID userId = UUID.randomUUID();

        willDoNothing().given(userValidator).validateEmailNotTaken(request.getEmail());
        given(keycloakService.createUser(request)).willReturn(keycloakId);

        willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", userId);
            return u;
        }).given(userRepository).saveAndFlush(any(User.class));

        given(userMapper.toResponse(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return UserResponse.builder()
                    .id(u.getId())
                    .keycloakId(u.getKeycloakId())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .email(u.getEmail())
                    .phoneNumber(u.getPhoneNumber())
                    .active(u.isActive())
                    .build();
        });

        // Act
        UserResponse response = userService.register(request);

        // Assert
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getKeycloakId()).isEqualTo(keycloakId);
        assertThat(response.getEmail()).isEqualTo("ada@n11.com");
        assertThat(response.getFirstName()).isEqualTo("Ada");
        assertThat(response.getLastName()).isEqualTo("Lovelace");

        then(keycloakService).should().createUser(request);
    }

    @Test
    @DisplayName("Register: email zaten varsa BusinessException fırlatır ve Keycloak çağrılmaz")
    void register_whenEmailAlreadyExists_shouldThrowBusinessException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@n11.com")
                .password("password123")
                .build();

        willThrow(new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS))
                .given(userValidator).validateEmailNotTaken(request.getEmail());

        // Act + Assert
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS)
                );

        then(keycloakService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Create: keycloakId ile user oluşturulunca response döner")
    void create_whenValid_shouldPersistAndReturnResponse() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .keycloakId("kc-999")
                .firstName("Grace")
                .lastName("Hopper")
                .email("grace@n11.com")
                .phoneNumber("+905550001122")
                .build();

        UUID userId = UUID.randomUUID();
        willDoNothing().given(userValidator).validateEmailNotTaken(request.getEmail());

        willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", userId);
            return u;
        }).given(userRepository).saveAndFlush(any(User.class));

        given(userMapper.toResponse(any(User.class))).willAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return UserResponse.builder()
                    .id(u.getId())
                    .keycloakId(u.getKeycloakId())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .email(u.getEmail())
                    .phoneNumber(u.getPhoneNumber())
                    .active(u.isActive())
                    .build();
        });

        // Act
        UserResponse response = userService.create(request);

        // Assert
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getKeycloakId()).isEqualTo("kc-999");
        assertThat(response.getEmail()).isEqualTo("grace@n11.com");
    }

    @Test
    @DisplayName("GetMe: aktif user bulunamazsa ResourceNotFoundException fırlatır")
    void getMe_whenUserNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        willThrow(new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND, userId))
                .given(userValidator).requireActiveUser(userId);

        // Act + Assert
        assertThatThrownBy(() -> userService.getMe(userId))
                .isInstanceOfSatisfying(ResourceNotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND)
                );
    }
}

