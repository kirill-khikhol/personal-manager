package com.personalassistant.user.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private UUID id;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfileEntity() {
        // Required by JPA
    }

    public UserProfileEntity(
            UUID id,
            String timeZone,
            String preferredLanguage,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.timeZone = timeZone;
        this.preferredLanguage = preferredLanguage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
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