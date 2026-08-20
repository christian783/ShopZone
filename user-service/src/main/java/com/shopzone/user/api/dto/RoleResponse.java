package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "RoleResponse")
public record RoleResponse(
        UUID id,
        String name,
        String description,
        Set<PrivilegeResponse> privileges
) {
}
