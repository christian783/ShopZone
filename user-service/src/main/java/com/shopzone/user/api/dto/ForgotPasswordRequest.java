package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ForgotPasswordRequest")
public record ForgotPasswordRequest(
        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email
) {
}
