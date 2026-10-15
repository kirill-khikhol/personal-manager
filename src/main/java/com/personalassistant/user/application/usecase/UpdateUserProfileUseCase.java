
package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateUserProfileUseCase {

    private final UserProfileRepository repository;

    public UpdateUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<UserProfile> execute(
            UUID id,
            String timeZone,
            String preferredLanguage
    ) {
        ZoneId.of(timeZone);

        String language = preferredLanguage.toLowerCase(Locale.ROOT);

        return repository.findById(id)
                .map(existing -> {
                    UserProfile updated = new UserProfile(
                            existing.getId(),
                            timeZone,
                            language,
                            existing.getCreatedAt(),
                            Instant.now()
                    );

                    return repository.save(updated);
                });
    }
}