package com.rysoo.api.security;

import java.util.Objects;
import java.util.UUID;

public record AuthenticatedUser(
    UUID userId,
    String email
) {
    public AuthenticatedUser {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }
}
