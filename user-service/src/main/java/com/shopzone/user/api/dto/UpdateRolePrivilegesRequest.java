package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

@Schema(name = "UpdateRolePrivilegesRequest")
public record UpdateRolePrivilegesRequest(
        @NotEmpty(message = "{role.privileges.required}")
        Set<UUID> privilegeIds
) {
}
