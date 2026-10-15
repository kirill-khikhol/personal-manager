package com.personalassistant.activity.domain;

import java.util.Objects;
import java.util.UUID;

public record ActivityId(UUID value) {

    public ActivityId {
        Objects.requireNonNull(value, "Activity id must not be null");
    }

    public static ActivityId generate() {
        return new ActivityId(UUID.randomUUID());
    }
}