package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest")
public record RegisterRequest(
        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        @Schema(example = "jane@shopzone.dev")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 8, message = "{user.password.size}")
        @Schema(example = "Secret123")
        String password,

        @NotBlank(message = "{user.fullName.required}")
        @Size(max = 200, message = "{user.fullName.size}")
        @Schema(example = "Jane Shopper")
        String fullName
) {
}
