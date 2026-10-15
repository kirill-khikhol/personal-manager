package com.personalassistant.user.application.port;

import com.personalassistant.user.domain.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {

    Optional<UserProfile> findById(UUID id);

    UserProfile save(UserProfile userProfile);
}