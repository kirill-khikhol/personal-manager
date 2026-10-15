package com.personalassistant.user.infrastructure.persistence;

import com.personalassistant.user.application.port.UserProfileRepository;
import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;
import com.personalassistant.user.infrastructure.UserProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserProfileRepositoryAdapter
        implements UserProfileRepository {

    private final SpringDataUserProfileRepository jpaRepository;
    private final UserProfileMapper mapper;

    public UserProfileRepositoryAdapter(
            SpringDataUserProfileRepository jpaRepository,
            UserProfileMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserProfile> findById(UserId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity entity = mapper.toEntity(userProfile);

        UserProfileEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }
}