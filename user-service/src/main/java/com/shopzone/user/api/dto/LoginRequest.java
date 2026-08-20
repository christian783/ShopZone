package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        @Schema(example = "admin@shopzone.dev")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Schema(example = "Admin123!")
        String password
) {
}
