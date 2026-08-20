package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ResetPasswordRequest")
public record ResetPasswordRequest(
        @NotBlank
        String token,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 8, message = "{user.password.size}")
        String newPassword
) {
}
