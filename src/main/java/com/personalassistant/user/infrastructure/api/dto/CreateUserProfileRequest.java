package com.personalassistant.user.infrastructure.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserProfileRequest(
        @NotBlank
        @Size(max = 64)
        String timeZone,

        @NotBlank
        @Size(max = 10)
        String preferredLanguage
) {
}