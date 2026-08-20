package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshRequest")
public record RefreshRequest(
        @NotBlank
        @Schema(example = "opaque-refresh-token")
        String refreshToken
) {
}
