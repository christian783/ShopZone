package com.shopzone.user.api;

import com.shopzone.user.api.dto.AuditLogResponse;
import com.shopzone.user.audit.AuditService;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.web.ApiMessages;
import com.shopzone.user.web.ApiResponse;
import com.shopzone.user.web.PageResponse;
import com.shopzone.user.web.SecurityUtils;
import com.shopzone.user.web.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Immutable audit trail of user and role mutations")
@SecurityRequirement(name = "bearer-jwt")
@StandardApiErrors
public class AuditController {

    private final AuditService auditService;
    private final ApiMessages messages;

    public AuditController(AuditService auditService, ApiMessages messages) {
        this.auditService = auditService;
        this.messages = messages;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.AUDIT_READ + "')")
    @Operation(operationId = "listAudit", summary = "List audit events",
            description = "Paginated audit log. Requires AUDIT_READ.")
    public ApiResponse<PageResponse<AuditLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        return ApiResponse.ok(messages.get("audit.list"),
                auditService.list(SecurityUtils.pageable(page, size, sort)));
    }
}
