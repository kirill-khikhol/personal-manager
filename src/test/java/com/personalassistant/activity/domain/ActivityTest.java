package com.personalassistant.activity.domain;

import com.personalassistant.user.domain.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {

    private Activity activity;

    @BeforeEach
    void setUp() {
        activity = Activity.createRoot(
                new UserId(UUID.randomUUID()),
                "Java preparation",
                "Prepare for backend interviews"
        );
    }

    @Test
    void createRoot_shouldUseDefaultValues() {
        assertNotNull(activity.getId());
        assertNull(activity.getParentId());
        assertEquals("Java preparation", activity.getName());
        assertEquals(ActivityStatus.ACTIVE, activity.getStatus());
        assertEquals(0.0, activity.getManualProgress());
        assertEquals(1.0, activity.getWeight());
    }

    @Test
    void setProgress_shouldAcceptValuesWithinRange() {
        activity.setManualProgress(42.5);

        assertEquals(42.5, activity.getManualProgress());

        activity.setManualProgress(100.0);
        assertEquals(100.0, activity.getManualProgress());

        activity.setManualProgress(0.0);
        assertEquals(0.0, activity.getManualProgress());
    }

    @Test
    void setProgress_shouldRejectInvalidValues() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setManualProgress(-0.1)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setManualProgress(100.1)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setManualProgress(Double.NaN)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setManualProgress(Double.POSITIVE_INFINITY)
                )
        );
    }

    @Test
    void setWeight_shouldRejectNonPositiveOrNonFiniteValues() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setWeight(0.0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setWeight(-1.0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setWeight(Double.NaN)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> activity.setWeight(Double.POSITIVE_INFINITY)
                )
        );
    }

    @Test
    void pauseAndResume_shouldChangeStatusWithoutChangingProgress() {
        activity.setManualProgress(35.5);

        activity.pause();

        assertEquals(ActivityStatus.PAUSED, activity.getStatus());
        assertEquals(35.5, activity.getManualProgress());

        activity.resume();

        assertEquals(ActivityStatus.ACTIVE, activity.getStatus());
        assertEquals(35.5, activity.getManualProgress());
    }

    @Test
    void resume_shouldRejectActivityThatIsNotPaused() {
        assertThrows(IllegalStateException.class, activity::resume);
    }

    @Test
    void complete_shouldSetStatusWithoutChangingManualProgress() {
        activity.setManualProgress(65.0);

        activity.complete();

        assertEquals(ActivityStatus.COMPLETED, activity.getStatus());
        assertEquals(65.0, activity.getManualProgress());
    }

    @Test
    void archive_shouldSetArchivedStatus() {
        activity.archive();

        assertEquals(ActivityStatus.ARCHIVED, activity.getStatus());
    }

    @Test
    void updateDetails_shouldTrimName() {
        activity.updateDetails("  Fitness  ", "Exercise regularly");

        assertEquals("Fitness", activity.getName());
        assertEquals("Exercise regularly", activity.getDescription());
    }

    @Test
    void updateDetails_shouldRejectBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> activity.updateDetails("  ", "Description")
        );
    }
}