package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "UserResponse")
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        boolean enabled,
        Set<String> roles,
        Set<String> privileges
) {
}
