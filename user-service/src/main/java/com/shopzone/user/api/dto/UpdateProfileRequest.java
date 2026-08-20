package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateProfileRequest")
public record UpdateProfileRequest(
        @NotBlank(message = "{user.fullName.required}")
        @Size(max = 200, message = "{user.fullName.size}")
        String fullName,

        @Size(min = 8, message = "{user.password.size}")
        String password
) {
}
