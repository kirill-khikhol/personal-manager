package com.personalassistant.user.infrastructure.api;

import com.personalassistant.user.application.usecase.GetUserProfileUseCase;
import com.personalassistant.user.application.usecase.UpdateUserProfileUseCase;
import com.personalassistant.user.domain.UserId;
import com.personalassistant.user.infrastructure.UserProfileMapper;
import com.personalassistant.user.infrastructure.api.dto.CreateUserProfileRequest;
import com.personalassistant.user.infrastructure.api.dto.UpdateUserProfileRequest;
import com.personalassistant.user.infrastructure.api.dto.UserProfileResponse;
import com.personalassistant.user.application.usecase.CreateUserProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final CreateUserProfileUseCase createUserProfileService;
    private final GetUserProfileUseCase getUserProfileService;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UserProfileMapper mapper;

    public UserProfileController(
            CreateUserProfileUseCase createUserProfileService,
            GetUserProfileUseCase getUserProfileService,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            UserProfileMapper mapper
    ) {
        this.createUserProfileService = createUserProfileService;
        this.getUserProfileService = getUserProfileService;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/profile")
    public UserProfileResponse getById(@RequestParam UUID id) {
        return getUserProfileService.execute(new UserId(id))
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"
                ));
    }

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse create(
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        return mapper.toResponse(
                createUserProfileService.execute(
                        request.timeZone(),
                        request.preferredLanguage()
                )
        );
    }

    @PutMapping("/profile")
    public UserProfileResponse update(
            @RequestParam UUID id,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return updateUserProfileUseCase.execute(
                        new UserId(id),
                        request.timeZone(),
                        request.preferredLanguage()
                )
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User profile not found"
                        )
                );
    }
}
