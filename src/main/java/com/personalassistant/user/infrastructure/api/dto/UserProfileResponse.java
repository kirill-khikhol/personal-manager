package com.personalassistant.user.infrastructure.api.dto;

import com.personalassistant.user.domain.UserProfile;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String timeZone,
        String preferredLanguage,
        Instant createdAt,
        Instant updatedAt
) {
}