package com.rysoo.api.controller;

import com.rysoo.api.dto.ApiResponse;
import com.rysoo.api.filter.CorrelationIdFilter;
import com.rysoo.api.security.AuthenticatedUser;
import com.rysoo.infrastructure.persistence.entity.ProfileEntity;
import com.rysoo.infrastructure.persistence.repository.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Authenticated User Profile Management")
@SecurityRequirement(name = "BearerAuth")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public record ProfileResponse(String id, String email, String fullName, String createdAt) {}

    @GetMapping
    @Operation(summary = "Get authenticated user profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@AuthenticationPrincipal Object principal) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        AuthenticatedUser user = resolveUser(principal);

        ProfileEntity profile = profileRepository.findById(user.userId())
                .orElseGet(() -> {
                    ProfileEntity newProfile = new ProfileEntity(user.userId(), user.email(), null);
                    return profileRepository.save(newProfile);
                });

        ProfileResponse response = new ProfileResponse(
                profile.getId().toString(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getCreatedAt().toString()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, correlationId));
    }

    private AuthenticatedUser resolveUser(Object principal) {
        if (principal instanceof AuthenticatedUser authUser) {
            return authUser;
        } else if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            UUID userId = UUID.fromString(jwt.getSubject());
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                email = "unknown@user.rysoo";
            }
            return new AuthenticatedUser(userId, email);
        }
        throw new IllegalArgumentException("Unsupported principal type: " + (principal != null ? principal.getClass() : "null"));
    }
}
