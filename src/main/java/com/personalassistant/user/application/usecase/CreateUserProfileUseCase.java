package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class CreateUserProfileUseCase {

    private final UserProfileRepository repository;

    public CreateUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserProfile execute(String timeZone, String preferredLanguage) {
        ZoneId.of(timeZone); // Throws if the time zone is invalid.

        String normalizedLanguage =
                preferredLanguage.toLowerCase(Locale.ROOT);

        Instant now = Instant.now();

        UserProfile profile = new UserProfile(
                UserId.generate(),
                timeZone,
                normalizedLanguage,
                now,
                now
        );

        return repository.save(profile);
    }
}