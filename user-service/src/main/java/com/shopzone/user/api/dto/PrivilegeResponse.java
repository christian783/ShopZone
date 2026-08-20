package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "PrivilegeResponse")
public record PrivilegeResponse(
        UUID id,
        String name,
        String description
) {
}
