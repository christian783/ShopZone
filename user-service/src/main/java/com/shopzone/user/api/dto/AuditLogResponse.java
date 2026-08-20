package com.shopzone.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Schema(name = "AuditLogResponse")
public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> metadata,
        Instant occurredAt
) {
}
