package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DateTimeException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateUserProfileUseCaseTest {

    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private CreateUserProfileUseCase useCase;

    @Test
    void shouldCreateAndSaveProfile() {
        when(repository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();

        UserProfile created = useCase.execute(
                "Asia/Jerusalem",
                "RU"
        );

        Instant after = Instant.now();

        ArgumentCaptor<UserProfile> captor =
                ArgumentCaptor.forClass(UserProfile.class);

        verify(repository).save(captor.capture());

        UserProfile saved = captor.getValue();

        assertNotNull(created.getId());
        assertEquals(created.getId(), saved.getId());
        assertEquals("Asia/Jerusalem", saved.getTimeZone());
        assertEquals("ru", saved.getPreferredLanguage());
        assertEquals(created.getCreatedAt(), created.getUpdatedAt());

        assertFalse(created.getCreatedAt().isBefore(before));
        assertFalse(created.getCreatedAt().isAfter(after));
    }

    @Test
    void shouldRejectInvalidTimeZone() {
        assertThrows(
                DateTimeException.class,
                () -> useCase.execute("Invalid/Zone", "ru")
        );

        verifyNoInteractions(repository);
    }
}
