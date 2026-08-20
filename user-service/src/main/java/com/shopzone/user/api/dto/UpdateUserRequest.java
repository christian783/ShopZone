package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

@Schema(name = "UpdateUserRequest")
public record UpdateUserRequest(
        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email,

        @Size(min = 8, message = "{user.password.size}")
        String password,

        @NotBlank(message = "{user.fullName.required}")
        @Size(max = 200, message = "{user.fullName.size}")
        String fullName,

        boolean enabled,

        @NotEmpty(message = "{user.roles.required}")
        Set<UUID> roleIds
) {
}
