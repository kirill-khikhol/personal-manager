package com.personalassistant.activity.domain;

import com.personalassistant.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTreeServiceTest {

    private UserId userId;
    private Activity root;
    private ActivityTree tree;
    private ActivityTreeService service;

    @BeforeEach
    void setUp() {
        userId = new UserId(UUID.randomUUID());
        root = Activity.createRoot(userId, "Root", null);
        tree = new ActivityTree(root.getId(), List.of(root));
        service = new ActivityTreeService();
    }

    @Test
    void addFirstChild_shouldTransferManualProgress() {
        root.setManualProgress(42.5);

        Activity child = service.addChild(
                tree, root.getId(), "Child", null
        );

        assertEquals(42.5, child.getManualProgress());
        assertEquals(ActivityStatus.ACTIVE, child.getStatus());
        assertEquals(1.0, child.getWeight());
    }

    @Test
    void addSecondNonArchivedChild_shouldStartWithZeroProgress() {
        root.setManualProgress(42.5);

        service.addChild(tree, root.getId(), "First", null);

        Activity second = service.addChild(
                tree, root.getId(), "Second", null
        );

        assertEquals(0.0, second.getManualProgress());
    }

    @Test
    void addChild_whenAllExistingChildrenAreArchived_shouldTransferManualProgress() {
        root.setManualProgress(42.5);

        Activity archivedChild = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Archived", null, ActivityStatus.ARCHIVED, 70.0, 1.0
        );

        tree.addActivity(archivedChild);

        Activity newChild = service.addChild(
                tree, root.getId(), "New child", null
        );

        assertEquals(42.5, newChild.getManualProgress());
    }

    @Test
    void addChild_toPausedParent_shouldCreatePausedChild() {
        root.pause();

        Activity child = service.addChild(
                tree, root.getId(), "Child", null
        );

        assertEquals(ActivityStatus.PAUSED, child.getStatus());
        assertEquals(ActivityStatus.PAUSED, root.getStatus());
    }

    @Test
    void addChild_toCompletedParent_shouldReactivateParentAndChild() {
        root.complete();

        Activity child = service.addChild(
                tree, root.getId(), "Child", null
        );

        assertEquals(ActivityStatus.ACTIVE, child.getStatus());
        assertEquals(ActivityStatus.ACTIVE, root.getStatus());
    }

    @Test
    void addChild_toArchivedParent_shouldPauseParentAndChild() {
        root.archive();

        Activity child = service.addChild(
                tree, root.getId(), "Child", null
        );

        assertEquals(ActivityStatus.PAUSED, child.getStatus());
        assertEquals(ActivityStatus.PAUSED, root.getStatus());
    }

    @Test
    void addChild_toCompletedParent_shouldReactivateCompletedAncestors() {
        Activity parent = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Parent", null, ActivityStatus.COMPLETED, 100.0, 1.0
        );

        tree.addActivity(parent);
        root.complete();

        Activity child = service.addChild(
                tree, parent.getId(), "Child", null
        );

        assertEquals(ActivityStatus.ACTIVE, child.getStatus());
        assertEquals(ActivityStatus.ACTIVE, parent.getStatus());
        assertEquals(ActivityStatus.ACTIVE, root.getStatus());
    }

    @Test
    void addChild_toArchivedParent_shouldPauseArchivedAncestors() {
        Activity parent = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Parent", null, ActivityStatus.ARCHIVED, 50.0, 1.0
        );

        tree.addActivity(parent);
        root.archive();

        Activity child = service.addChild(
                tree, parent.getId(), "Child", null
        );

        assertEquals(ActivityStatus.PAUSED, child.getStatus());
        assertEquals(ActivityStatus.PAUSED, parent.getStatus());
        assertEquals(ActivityStatus.PAUSED, root.getStatus());
    }

    @Test
    void addFirstChild_shouldPreserveParentProgressThroughChild() {
        root.setManualProgress(60.0);

        Activity child = service.addChild(
                tree, root.getId(), "First", null
        );

        assertEquals(60.0, child.getManualProgress());
        assertEquals(60.0, tree.getProgress(root.getId()));
    }

    @Test
    void addSecondChild_shouldRecalculateParentProgress() {
        root.setManualProgress(60.0);

        service.addChild(tree, root.getId(), "First", null);
        service.addChild(tree, root.getId(), "Second", null);

        assertEquals(30.0, tree.getProgress(root.getId()));
    }
    @Test
    void getProgress_shouldUseChildWeights() {
        UserId userId = new UserId(UUID.randomUUID());

        Activity root = Activity.createRoot(userId, "Learning", null);

        Activity first = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Java", null, ActivityStatus.ACTIVE, 30.0, 3.0
        );

        Activity second = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Fitness", null, ActivityStatus.ACTIVE, 60.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, first, second)
        );

        assertEquals(37.5, tree.getProgress(root.getId()), 0.0001);
    }

    @Test
    void pauseSubtree_shouldPauseAllActiveDescendants() {
        Activity child = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Child", null, ActivityStatus.ACTIVE, 40.0, 1.0
        );

        Activity grandchild = new Activity(
                ActivityId.generate(), userId, child.getId(),
                "Grandchild", null, ActivityStatus.ACTIVE, 60.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, child, grandchild)
        );

        service.pauseSubtree(tree, root.getId());

        assertEquals(ActivityStatus.PAUSED, root.getStatus());
        assertEquals(ActivityStatus.PAUSED, child.getStatus());
        assertEquals(ActivityStatus.PAUSED, grandchild.getStatus());
    }

    @Test
    void pauseSubtree_shouldPreserveCompletedAndArchivedStatuses() {
        Activity completed = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Completed", null, ActivityStatus.COMPLETED, 30.0, 1.0
        );

        Activity archived = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Archived", null, ActivityStatus.ARCHIVED, 50.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, completed, archived)
        );

        service.pauseSubtree(tree, root.getId());

        assertEquals(ActivityStatus.PAUSED, root.getStatus());
        assertEquals(ActivityStatus.COMPLETED, completed.getStatus());
        assertEquals(ActivityStatus.ARCHIVED, archived.getStatus());
    }

    @Test
    void pauseSubtree_shouldPreserveProgress() {
        Activity child = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Child", null, ActivityStatus.ACTIVE, 60.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, child)
        );

        double rootProgressBefore = tree.getProgress(root.getId());
        double childProgressBefore = tree.getProgress(child.getId());

        service.pauseSubtree(tree, root.getId());

        assertEquals(rootProgressBefore, tree.getProgress(root.getId()));
        assertEquals(childProgressBefore, tree.getProgress(child.getId()));
    }

    @Test
    void resumeActivity_shouldResumeOnlySelectedActivity() {
        root.pause();

        Activity child = new Activity(
                ActivityId.generate(),
                userId,
                root.getId(),
                "Child",
                null,
                ActivityStatus.PAUSED,
                40.0,
                1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(),
                List.of(root, child)
        );

        service.resumeActivity(tree, root.getId());

        assertEquals(ActivityStatus.ACTIVE, root.getStatus());
        assertEquals(ActivityStatus.PAUSED, child.getStatus());
    }

    @Test
    void resumeActivity_shouldPreserveProgress() {
        UserId userId = UserId.generate();

        Activity root = Activity.createRoot(userId, "Root", null);
        root.setManualProgress(65.0);
        root.pause();

        ActivityTree tree = new ActivityTree(
                root.getId(),
                List.of(root)
        );

        service.resumeActivity(tree, root.getId());

        assertEquals(ActivityStatus.ACTIVE, root.getStatus());
        assertEquals(65.0, root.getManualProgress());
    }

    @Test
    void completeActivity_shouldCompleteLeaf() {
        root.setManualProgress(45.0);

        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        service.completeActivity(tree, root.getId());

        assertEquals(ActivityStatus.COMPLETED, root.getStatus());
        assertEquals(100.0, tree.getProgress(root.getId()));
    }

    @Test
    void completeActivity_shouldCompleteParentWhenAllChildrenAreCompletedOrArchived() {
        Activity completedChild = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Completed child", null, ActivityStatus.COMPLETED, 30.0, 1.0
        );

        Activity archivedChild = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Archived child", null, ActivityStatus.ARCHIVED, 50.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, completedChild, archivedChild)
        );

        service.completeActivity(tree, root.getId());

        assertEquals(ActivityStatus.COMPLETED, root.getStatus());
        assertEquals(100.0, tree.getProgress(root.getId()));
    }

    @Test
    void completeActivity_shouldRejectParentWithUnfinishedChild() {
        Activity activeChild = new Activity(
                ActivityId.generate(), userId, root.getId(),
                "Active child", null, ActivityStatus.ACTIVE, 40.0, 1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(), List.of(root, activeChild)
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.completeActivity(tree, root.getId())
        );

        assertEquals(ActivityStatus.ACTIVE, root.getStatus());
    }

    @Test
    void completeActivity_shouldRejectArchivedActivity() {
        root.archive();

        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        assertThrows(
                IllegalStateException.class,
                () -> service.completeActivity(tree, root.getId())
        );

        assertEquals(ActivityStatus.ARCHIVED, root.getStatus());
    }

    @Test
    void archiveActivity_shouldChangeStatusToArchived() {
        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        service.archiveActivity(tree, root.getId());

        assertEquals(ActivityStatus.ARCHIVED, root.getStatus());
    }

    @Test
    void archiveActivity_shouldBeIdempotent() {
        ActivityTree tree = new ActivityTree(root.getId(), List.of(root));

        service.archiveActivity(tree, root.getId());

        assertDoesNotThrow(
                () -> service.archiveActivity(tree, root.getId())
        );

        assertEquals(ActivityStatus.ARCHIVED, root.getStatus());
    }

    @Test
    void archiveActivity_shouldExcludeArchivedChildFromParentProgress() {
        Activity firstChild = new Activity(
                ActivityId.generate(),
                userId,
                root.getId(),
                "First child",
                null,
                ActivityStatus.ACTIVE,
                20.0,
                1.0
        );

        Activity secondChild = new Activity(
                ActivityId.generate(),
                userId,
                root.getId(),
                "Second child",
                null,
                ActivityStatus.ACTIVE,
                80.0,
                1.0
        );

        ActivityTree tree = new ActivityTree(
                root.getId(),
                List.of(root, firstChild, secondChild)
        );

        assertEquals(50.0, tree.getProgress(root.getId()));

        service.archiveActivity(tree, secondChild.getId());

        assertEquals(ActivityStatus.ARCHIVED, secondChild.getStatus());
        assertEquals(20.0, tree.getProgress(root.getId()));
    }
}
