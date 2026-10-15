package com.personalassistant.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataUserProfileRepository
        extends JpaRepository<UserProfileEntity, UUID> {
}