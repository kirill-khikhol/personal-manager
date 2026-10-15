package com.personalassistant.user.application.port;

import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    Optional<UserProfile> findById(UserId id);

    UserProfile save(UserProfile userProfile);
}