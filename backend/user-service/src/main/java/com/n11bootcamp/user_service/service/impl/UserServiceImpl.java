package com.n11bootcamp.user_service.service.impl;

import com.n11bootcamp.common.response.PageResponse;
import com.n11bootcamp.user_service.dto.request.CreateUserRequest;
import com.n11bootcamp.user_service.dto.request.RegisterRequest;
import com.n11bootcamp.user_service.dto.request.UpdateUserRequest;
import com.n11bootcamp.user_service.dto.response.UserResponse;
import com.n11bootcamp.user_service.entity.User;
import com.n11bootcamp.user_service.mapper.UserMapper;
import com.n11bootcamp.user_service.repository.UserRepository;
import com.n11bootcamp.user_service.service.KeycloakService;
import com.n11bootcamp.user_service.service.UserService;
import com.n11bootcamp.user_service.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(userValidator.requireActiveUser(id));
    }

    @Override
    public UserResponse getMe(UUID userId) {
        return userMapper.toResponse(userValidator.requireActiveUser(userId));
    }

    @Override
    public UserResponse getDeletedById(UUID id) {
        return userMapper.toResponse(userValidator.requireDeletedUser(id));
    }

    @Override
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.of(
                userRepository.findAllByDeletedFalse(pageable)
                              .map(userMapper::toResponse)
        );
    }

    @Override
    public UserResponse getByEmail(String email) {
        return userMapper.toResponse(userValidator.requireActiveUserByEmail(email));
    }

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        userValidator.validateEmailNotTaken(request.getEmail());
    
        User user = User.builder()
                .keycloakId(request.getKeycloakId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    
        User saved = userRepository.saveAndFlush(user);
        log.info("User created: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userValidator.requireActiveUser(id);
        applyUpdates(user, request);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userValidator.requireActiveUser(id);
        user.delete();
        userRepository.saveAndFlush(user);
        log.info("User soft deleted: {}", id);
    }

    @Override
    @Transactional
    public UserResponse restore(UUID id) {
        User user = userValidator.requireDeletedUser(id);
        user.restore();
        log.info("User restored: {}", id);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        userValidator.validateEmailNotTaken(request.getEmail());

        String keycloakId = keycloakService.createUser(request);

        User user = User.builder()
                .keycloakId(keycloakId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User saved = userRepository.saveAndFlush(user);
        log.info("User registered: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void applyUpdates(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null)   user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)    user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userValidator.validateEmailNotTaken(request.getEmail());
            user.setEmail(request.getEmail());
        }

    }
}