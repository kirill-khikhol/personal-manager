package com.personalassistant.activity.domain;

import java.util.*;

public class ActivityTree {

    private final ActivityId rootId;
    private final Map<ActivityId, Activity> activities;
    private final Map<ActivityId, List<ActivityId>> childrenByParent;

    public ActivityTree(
            ActivityId rootId,
            Collection<Activity> activities
    ) {
        this.rootId = Objects.requireNonNull(
                rootId, "Root id must not be null"
        );

        Objects.requireNonNull(
                activities, "Activities must not be null"
        );

        this.activities = new HashMap<>();

        for (Activity activity : activities) {
            Objects.requireNonNull(
                    activity, "Activity must not be null"
            );

            Activity previous = this.activities.put(
                    activity.getId(), activity
            );

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Duplicate activity id: " + activity.getId()
                );
            }
        }

        this.childrenByParent = new HashMap<>();

        for (Activity activity : this.activities.values()) {
            childrenByParent.put(activity.getId(), new ArrayList<>());
        }

        for (Activity activity : this.activities.values()) {
            ActivityId parentId = activity.getParentId();

            if (parentId != null && childrenByParent.containsKey(parentId)) {
                childrenByParent.get(parentId).add(activity.getId());
            }
        }

        Activity root = this.activities.get(rootId);

        if (root == null) {
            throw new IllegalArgumentException(
                    "Root activity not found"
            );
        }

        if (root.getParentId() != null) {
            throw new IllegalArgumentException(
                    "Root activity must not have a parent"
            );
        }

        validateTree();
    }

    public Activity getRoot() {
        return activities.get(rootId);
    }

    public Activity getActivity(ActivityId id) {
        Activity activity = activities.get(id);

        if (activity == null) {
            throw new IllegalArgumentException(
                    "Activity not found: " + id
            );
        }

        return activity;
    }

    public Collection<Activity> getActivities() {
        return List.copyOf(activities.values());
    }

    private void validateTree() {
        var root = getRoot();
        var userId = root.getUserId();

        for (Activity activity : activities.values()) {
            if (!userId.equals(activity.getUserId())) {
                throw new IllegalArgumentException(
                        "All activities in a tree must belong to the same user"
                );
            }

            ActivityId parentId = activity.getParentId();

            if (activity.getId().equals(parentId)) {
                throw new IllegalArgumentException(
                        "Activity cannot be its own parent"
                );
            }

            if (parentId != null && !activities.containsKey(parentId)) {
                throw new IllegalArgumentException(
                        "Parent activity not found: " + parentId
                );
            }
        }

        var visited = new HashSet<ActivityId>();
        var queue = new ArrayDeque<ActivityId>();

        queue.add(rootId);

        while (!queue.isEmpty()) {
            ActivityId currentId = queue.removeFirst();

            if (!visited.add(currentId)) {
                throw new IllegalArgumentException(
                        "Cycle or duplicate child link detected"
                );
            }

            for (ActivityId childId : childrenByParent.get(currentId)) {
                Activity child = activities.get(childId);

                if (child == null || !currentId.equals(child.getParentId())) {
                    throw new IllegalArgumentException(
                            "Inconsistent parent-child relationship"
                    );
                }

                queue.addLast(childId);
            }
        }

        if (visited.size() != activities.size()) {
            throw new IllegalArgumentException(
                    "Some activities are not reachable from the root"
            );
        }
    }

    public void addActivity(Activity activity) {
        Objects.requireNonNull(activity, "Activity must not be null");

        if (!activity.getUserId().equals(getRoot().getUserId())) {
            throw new IllegalArgumentException(
                    "Activity must belong to the same user as the tree"
            );
        }

        if (activities.containsKey(activity.getId())) {
            throw new IllegalArgumentException(
                    "Activity already exists: " + activity.getId()
            );
        }

        if (activity.getParentId() == null
                || !activities.containsKey(activity.getParentId())) {
            throw new IllegalArgumentException(
                    "Parent activity must exist in the tree"
            );
        }

        activities.put(activity.getId(), activity);

        childrenByParent.put(activity.getId(), new ArrayList<>());
        childrenByParent.get(activity.getParentId()).add(activity.getId());
    }

    public Activity getParent(Activity activity) {
        ActivityId parentId = activity.getParentId();

        return parentId == null ? null : getActivity(parentId);
    }

    public List<Activity> getChildren(ActivityId parentId) {
        getActivity(parentId);

        return childrenByParent.get(parentId).stream()
                .map(activities::get)
                .toList();
    }

    public boolean hasNonArchivedChildren(ActivityId parentId) {
        return getChildren(parentId).stream()
                .anyMatch(child -> child.getStatus() != ActivityStatus.ARCHIVED);
    }

    public double getProgress(ActivityId activityId) {
        Activity activity = getActivity(activityId);

        if (activity.getStatus() == ActivityStatus.COMPLETED) {
            return 100.0;
        }

        var allChildren = getChildren(activityId);

        var children = allChildren.stream()
                .filter(child -> child.getStatus() != ActivityStatus.ARCHIVED)
                .toList();

        if (children.isEmpty()) {
            return allChildren.isEmpty()
                    ? activity.getManualProgress()
                    : 0.0;
        }

        double weightedProgressSum = 0.0;
        double totalWeight = 0.0;

        for (Activity child : children) {
            weightedProgressSum += getProgress(child.getId()) * child.getWeight();
            totalWeight += child.getWeight();
        }

        return weightedProgressSum / totalWeight;
    }


}