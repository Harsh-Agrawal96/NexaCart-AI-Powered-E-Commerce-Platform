package com.n11bootcamp.user_service.validator;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.n11bootcamp.common.exception.BusinessException;
import com.n11bootcamp.common.exception.ResourceNotFoundException;
import com.n11bootcamp.user_service.entity.User;
import com.n11bootcamp.user_service.exception.UserErrorCode;
import com.n11bootcamp.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmailAndDeletedFalse(email)) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    public User requireActiveUser(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND, id));
    }

    public User requireDeletedUser(UUID id) {
        return userRepository.findByIdAndDeletedTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_ALREADY_DELETED, id));
    }

    public User requireActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND, email));
    }

    public User requireActiveUserByKeycloakId(String keycloakId) {
    return userRepository.findByKeycloakIdAndDeletedFalse(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException(UserErrorCode.USER_NOT_FOUND, keycloakId));
    }
}
