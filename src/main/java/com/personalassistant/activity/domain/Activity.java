package com.personalassistant.activity.domain;

import com.personalassistant.user.domain.UserId;

import java.util.Objects;

public class Activity {

    private final ActivityId id;
    private final UserId userId;

    private ActivityId parentId;
    private String name;
    private String description;
    private ActivityStatus status;
    private double manualProgress;
    private double weight;

    public Activity(
            ActivityId id,
            UserId userId,
            ActivityId parentId,
            String name,
            String description,
            ActivityStatus status,
            double manualProgress,
            double weight
    ) {
        this.id = Objects.requireNonNull(id, "Activity id must not be null");
        this.userId = Objects.requireNonNull(userId, "User id must not be null");
        this.parentId = parentId;

        setName(name);
        this.description = description;
        this.status = Objects.requireNonNull(
                status, "Activity status must not be null"
        );

        setManualProgress(manualProgress);
        setWeight(weight);
    }

    public static Activity createRoot(
            UserId userId,
            String name,
            String description
    ) {
        return new Activity(
                ActivityId.generate(),
                userId,
                null,
                name,
                description,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );
    }

    public void pause() {
        if (status == ActivityStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Archived activity cannot be paused directly"
            );
        }

        if (status == ActivityStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed activity cannot be paused"
            );
        }

        status = ActivityStatus.PAUSED;
    }

    public void resume() {
        if (status != ActivityStatus.PAUSED) {
            throw new IllegalStateException(
                    "Only paused activity can be resumed"
            );
        }

        status = ActivityStatus.ACTIVE;
    }

    public void complete() {
        if (status == ActivityStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Archived activity cannot be completed"
            );
        }

        status = ActivityStatus.COMPLETED;
    }

    public void archive() {
        status = ActivityStatus.ARCHIVED;
    }

    public void setManualProgress(double progress) {
        if (!Double.isFinite(progress) || progress < 0.0 || progress > 100.0) {
            throw new IllegalArgumentException(
                    "Progress must be finite and between 0 and 100"
            );
        }

        this.manualProgress = progress;
    }

    public void setWeight(double weight) {
        if (!Double.isFinite(weight) || weight <= 0.0) {
            throw new IllegalArgumentException(
                    "Weight must be finite and greater than zero"
            );
        }

        this.weight = weight;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Activity name must not be blank"
            );
        }

        this.name = name.trim();
    }

    public void updateDetails(String name, String description) {
        setName(name);
        this.description = description;
    }

    void reactivateFromTree() {
        if (status == ActivityStatus.COMPLETED) {
            status = ActivityStatus.ACTIVE;
        }
    }

    void pauseFromTree() {
        if (status == ActivityStatus.ARCHIVED) {
            status = ActivityStatus.PAUSED;
        }
    }

    public ActivityId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public ActivityId getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public double getManualProgress() {
        return manualProgress;
    }

    public double getWeight() {
        return weight;
    }
}