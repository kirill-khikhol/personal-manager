package com.personalassistant.user.infrastructure;

import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.domain.UserProfile;
import com.personalassistant.user.infrastructure.api.dto.UserProfileResponse;
import com.personalassistant.user.infrastructure.persistence.UserProfileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserProfileMapper {

    UserProfile toDomain(UserProfileEntity entity);

    UserProfileEntity toEntity(UserProfile profile);

    UserProfileResponse toResponse(UserProfile profile);

    default UserId toUserId(UUID value) {
        return value == null ? null : new UserId(value);
    }

    default UUID toUuid(UserId id) {
        return id == null ? null : id.value();
    }
}