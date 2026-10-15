
package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private UpdateUserProfileUseCase useCase;

    @Test
    void shouldUpdateExistingProfile() {
        UserId id = UserId.generate();
        Instant createdAt = Instant.parse("2025-01-01T10:00:00Z");
        Instant oldUpdatedAt = Instant.parse("2025-01-02T10:00:00Z");

        UserProfile existing = new UserProfile(
                id,
                "Asia/Jerusalem",
                "ru",
                createdAt,
                oldUpdatedAt
        );

        when(repository.findById(id))
                .thenReturn(Optional.of(existing));

        when(repository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<UserProfile> result = useCase.execute(
                id,
                "Europe/London",
                "EN"
        );

        assertTrue(result.isPresent());

        UserProfile updated = result.orElseThrow();

        assertNotSame(existing, updated);
        assertEquals(id, updated.getId());
        assertEquals("Europe/London", updated.getTimeZone());
        assertEquals("en", updated.getPreferredLanguage());
        assertEquals(createdAt, updated.getCreatedAt());
        assertFalse(updated.getUpdatedAt().isBefore(oldUpdatedAt));

        verify(repository).findById(id);
        verify(repository).save(updated);
    }

    @Test
    void shouldNotSaveWhenProfileDoesNotExist() {
        UserId id = UserId.generate();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        Optional<UserProfile> result = useCase.execute(
                id,
                "Europe/London",
                "en"
        );

        assertTrue(result.isEmpty());

        verify(repository).findById(id);
        verify(repository, never()).save(any(UserProfile.class));
    }

    @Test
    void shouldRejectInvalidTimeZoneWithoutAccessingRepository() {
        assertThrows(
                DateTimeException.class,
                () -> useCase.execute(
                        UserId.generate(),
                        "Invalid/Zone",
                        "en"
                )
        );

        verifyNoInteractions(repository);
    }
}
