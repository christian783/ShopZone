package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateRoleRequest")
public record CreateRoleRequest(
        @NotBlank(message = "{role.name.required}")
        @Size(max = 64)
        String name,

        @Size(max = 255)
        String description
) {
}
