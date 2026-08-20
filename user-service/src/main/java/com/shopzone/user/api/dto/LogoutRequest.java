package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LogoutRequest")
public record LogoutRequest(
        @NotBlank
        String refreshToken
) {
}
