package com.shopzone.user.api;

import com.shopzone.user.api.dto.PrivilegeResponse;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.privilege.PrivilegeService;
import com.shopzone.user.web.ApiMessages;
import com.shopzone.user.web.ApiResponse;
import com.shopzone.user.web.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/privileges")
@Tag(name = "Privileges", description = "Seeded privilege catalog (read-only)")
@SecurityRequirement(name = "bearer-jwt")
@StandardApiErrors
public class PrivilegeController {

    private final PrivilegeService privilegeService;
    private final ApiMessages messages;

    public PrivilegeController(PrivilegeService privilegeService, ApiMessages messages) {
        this.privilegeService = privilegeService;
        this.messages = messages;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.PRIVILEGE_READ + "')")
    @Operation(operationId = "listPrivileges", summary = "List privileges",
            description = "Read-only catalog. Privileges are shared across roles. Requires PRIVILEGE_READ.")
    public ApiResponse<List<PrivilegeResponse>> list() {
        return ApiResponse.ok(messages.get("privilege.list"), privilegeService.list());
    }
}
