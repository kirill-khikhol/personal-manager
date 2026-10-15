package com.personalassistant.user.application.usecase;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GetUserProfileUseCase {

    private final UserProfileRepository repository;

    public GetUserProfileUseCase(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> execute(UserId id) {
        return repository.findById(id);
    }
}
