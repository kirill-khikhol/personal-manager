package com.personalassistant.activity.domain;

public class ActivityTreeService {

    public Activity addChild(
            ActivityTree tree,
            ActivityId parentId,
            String name,
            String description
    ) {
        Activity parent = tree.getActivity(parentId);

        double initialProgress = tree.hasNonArchivedChildren(parent.getId())
                ? 0.0
                : parent.getManualProgress();

        ActivityStatus parentStatusBefore = parent.getStatus();

        ActivityStatus childStatus = switch (parentStatusBefore) {
            case ACTIVE -> ActivityStatus.ACTIVE;
            case PAUSED -> ActivityStatus.PAUSED;
            case COMPLETED -> ActivityStatus.ACTIVE;
            case ARCHIVED -> ActivityStatus.PAUSED;
        };

        Activity child = new Activity(
                ActivityId.generate(),
                parent.getUserId(),
                parent.getId(),
                name,
                description,
                childStatus,
                initialProgress,
                1.0
        );

        tree.addActivity(child);

        switch (parent.getStatus()) {
            case COMPLETED -> reactivateCompletedAncestors(tree, parent);
            case ARCHIVED -> pauseArchivedAncestors(tree, parent);
            case ACTIVE, PAUSED -> {
                // Parent and ancestors keep their current statuses.
            }
        }

        return child;
    }

    private void reactivateCompletedAncestors(
            ActivityTree tree,
            Activity activity
    ) {
        Activity current = activity;

        while (current != null) {
            current.reactivateFromTree();
            current = tree.getParent(current);
        }
    }

    private void pauseArchivedAncestors(
            ActivityTree tree,
            Activity activity
    ) {
        Activity current = activity;

        while (current != null) {
            current.pauseFromTree();
            current = tree.getParent(current);
        }
    }

    public void pauseSubtree(ActivityTree tree, ActivityId activityId) {
        Activity activity = tree.getActivity(activityId);

        pauseRecursively(tree, activity);
    }

    private void pauseRecursively(ActivityTree tree, Activity activity) {
        if (activity.getStatus() == ActivityStatus.ACTIVE) {
            activity.pause();
        }

        for (Activity child : tree.getChildren(activity.getId())) {
            pauseRecursively(tree, child);
        }
    }

    public void resumeActivity(ActivityTree tree, ActivityId activityId) {
        Activity activity = tree.getActivity(activityId);
        activity.resume();
    }

    public void completeActivity(ActivityTree tree, ActivityId activityId) {
        Activity activity = tree.getActivity(activityId);

        if (activity.getStatus() == ActivityStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Cannot complete an archived activity"
            );
        }

        boolean hasUnfinishedChildren = tree.getChildren(activityId).stream()
                .anyMatch(child ->
                        child.getStatus() != ActivityStatus.COMPLETED
                                && child.getStatus() != ActivityStatus.ARCHIVED
                );

        if (hasUnfinishedChildren) {
            throw new IllegalStateException(
                    "Cannot complete an activity with unfinished children"
            );
        }

        activity.complete();
    }

    public void archiveActivity(ActivityTree tree, ActivityId activityId) {
        Activity activity = tree.getActivity(activityId);

        if (activity.getStatus() != ActivityStatus.ARCHIVED) {
            activity.archive();
        }
    }
}