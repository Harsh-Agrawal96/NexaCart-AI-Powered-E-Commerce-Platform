package com.n11bootcamp.user_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.n11bootcamp.user_service.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndDeletedFalse(UUID id);

    Optional<User> findByIdAndDeletedTrue(UUID id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    boolean existsByEmailAndDeletedFalse(String email);

    Optional<User> findByKeycloakIdAndDeletedFalse(String keycloakId);

}
