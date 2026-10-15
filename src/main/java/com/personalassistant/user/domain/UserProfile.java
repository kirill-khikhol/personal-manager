package com.personalassistant.user.domain;

import java.time.Instant;
import java.util.Objects;

public class UserProfile {

    private final UserId id;
    private final String timeZone;
    private final String preferredLanguage;
    private final Instant createdAt;
    private final Instant updatedAt;

    public UserProfile(
            UserId id,
            String timeZone,
            String preferredLanguage,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.timeZone = requireNotBlank(timeZone, "timeZone");
        this.preferredLanguage =
                requireNotBlank(preferredLanguage, "preferredLanguage");
        this.createdAt =
                Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt =
                Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    private static String requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    public UserId getId() {
        return id;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}