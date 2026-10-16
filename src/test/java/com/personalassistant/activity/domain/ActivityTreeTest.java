package com.personalassistant.activity.domain;

import com.personalassistant.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityTreeTest {

    private UserId userId;
    private Activity root;

    @BeforeEach
    void setUp() {
        userId = new UserId(UUID.randomUUID());
        root = Activity.createRoot(userId, "Root", null);
    }

    @Test
    void getProgress_shouldReturnManualProgressForLeaf() {
        root.setManualProgress(42.5);

        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        assertEquals(42.5, tree.getProgress(root.getId()));
    }

    @Test
    void getProgress_shouldCalculateWeightedAverage() {
        Activity java = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Java", null, ActivityStatus.ACTIVE, 40.0, 2.0
        );

        Activity fitness = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Fitness", null, ActivityStatus.ACTIVE, 80.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, java, fitness)
        );

        assertEquals(
                160.0 / 3.0,
                tree.getProgress(root.getId()),
                0.0001
        );
    }

    @Test
    void getProgress_shouldIgnoreArchivedChildren() {
        Activity active = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Active", null, ActivityStatus.ACTIVE, 60.0, 1.0
        );

        Activity archived = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Archived", null, ActivityStatus.ARCHIVED, 90.0, 10.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, active, archived)
        );

        assertEquals(60.0, tree.getProgress(root.getId()));
    }

    @Test
    void getProgress_shouldReturnZeroWhenAllChildrenAreArchived() {
        root.setManualProgress(75.0);

        Activity archived = new Activity(
                ActivityId.generate(),
                userId,
                root.getId(),
                "Old activity",
                null,
                ActivityStatus.ARCHIVED,
                90.0,
                1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(),
                List.of(root, archived)
        );

        assertEquals(0.0, tree.getProgress(root.getId()));
    }

    @Test
    void constructor_shouldRejectActivityOutsideRootTree() {
        Activity anotherRoot = Activity.createRoot(
                userId, "Another root", null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityTree(
                        root.getId(),
                        List.of(root, anotherRoot)
                )
        );
    }

    @Test
    void getProgress_shouldCountCompletedChildAsOneHundredPercent() {
        Activity completed = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Completed", null, ActivityStatus.COMPLETED, 30.0, 1.0
        );

        Activity active = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Active", null, ActivityStatus.ACTIVE, 20.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, completed, active)
        );

        assertEquals(60.0, tree.getProgress(root.getId()));
    }

    @Test
    void addActivity_shouldRejectDuplicateId() {
        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        Activity duplicate = new Activity(
                root.getId(),
                userId,
                null,
                "Duplicate",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tree.addActivity(duplicate)
        );
    }

    @Test
    void addActivity_shouldRejectUnknownParent() {
        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        Activity child = new Activity(
                ActivityId.generate(),
                userId,
                ActivityId.generate(),
                "Child",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tree.addActivity(child)
        );
    }

    @Test
    void constructor_shouldRejectActivitiesFromDifferentUsers() {
        UserId rootUserId = UserId.generate();
        UserId anotherUserId = UserId.generate();

        Activity root = Activity.createRoot(rootUserId, "Root", null);

        Activity child = new Activity(
                ActivityId.generate(),
                anotherUserId,
                root.getId(),
                "Child",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityTree(root.getId(), List.of(root, child))
        );
    }

    @Test
    void addActivity_shouldRejectActivityFromAnotherUser() {
        UserId rootUserId = UserId.generate();
        UserId anotherUserId = UserId.generate();

        Activity root = Activity.createRoot(rootUserId, "Root", null);
        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        Activity child = new Activity(
                ActivityId.generate(),
                anotherUserId,
                root.getId(),
                "Child",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tree.addActivity(child)
        );

        assertEquals(0, tree.getChildren(root.getId()).size());
    }

    @Test
    void constructor_shouldRejectUnknownRootId() {
        UserId userId = UserId.generate();
        Activity root = Activity.createRoot(userId, "Root", null);

        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityTree(
                        ActivityId.generate(),
                        List.of(root)
                )
        );
    }

    @Test
    void constructor_shouldRejectCycle() {
        UserId userId = UserId.generate();

        ActivityId firstId = ActivityId.generate();
        ActivityId secondId = ActivityId.generate();

        Activity first = new Activity(
                firstId,
                userId,
                secondId,
                "First",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        Activity second = new Activity(
                secondId,
                userId,
                firstId,
                "Second",
                null,
                ActivityStatus.ACTIVE,
                0.0,
                1.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ActivityTree(firstId, List.of(first, second))
        );
    }
}