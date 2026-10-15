package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetUserProfileUseCaseTest {
    @Mock
    private UserProfileRepository repository;

    @InjectMocks
    private GetUserProfileUseCase service;

    @Test
    void shouldReturnProfileWhenItExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        UserProfile profile = new UserProfile(
                id,
                "Asia/Jerusalem",
                "ru",
                now,
                now
        );

        when(repository.findById(id))
                .thenReturn(Optional.of(profile));

        Optional<UserProfile> result = service.execute(id);

        assertTrue(result.isPresent());
        assertSame(profile, result.orElseThrow());

        verify(repository).findById(id);
    }

    @Test
    void shouldReturnEmptyWhenProfileDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        Optional<UserProfile> result = service.execute(id);

        assertTrue(result.isEmpty());

        verify(repository).findById(id);
    }
}
