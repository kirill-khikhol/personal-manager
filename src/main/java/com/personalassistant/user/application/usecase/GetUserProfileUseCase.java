package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetUserProfileUseCase {

    private final UserProfileRepository repository;

    public GetUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> execute(UUID id) {
        return repository.findById(id);
    }
}
