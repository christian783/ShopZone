package com.shopzone.user.api;

import com.shopzone.user.api.dto.CreateRoleRequest;
import com.shopzone.user.api.dto.RoleResponse;
import com.shopzone.user.api.dto.UpdateRolePrivilegesRequest;
import com.shopzone.user.api.dto.UpdateRoleRequest;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.role.RoleService;
import com.shopzone.user.web.ApiMessages;
import com.shopzone.user.web.ApiResponse;
import com.shopzone.user.web.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Roles and many-to-many privilege assignment")
@SecurityRequirement(name = "bearer-jwt")
@StandardApiErrors
public class RoleController {

    private final RoleService roleService;
    private final ApiMessages messages;

    public RoleController(RoleService roleService, ApiMessages messages) {
        this.roleService = roleService;
        this.messages = messages;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_READ + "')")
    @Operation(operationId = "listRoles", summary = "List roles", description = "Requires ROLE_READ.")
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok(messages.get("role.list"), roleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_READ + "')")
    @Operation(operationId = "getRole", summary = "Get role", description = "Requires ROLE_READ.")
    public ApiResponse<RoleResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(messages.get("role.get"), roleService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_MANAGE + "')")
    @Operation(operationId = "createRole", summary = "Create role", description = "Requires ROLE_MANAGE. Audited.")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(messages.get("role.created"), roleService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_MANAGE + "')")
    @Operation(operationId = "updateRole", summary = "Update role", description = "Requires ROLE_MANAGE. Audited.")
    public ApiResponse<RoleResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok(messages.get("role.updated"), roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_MANAGE + "')")
    @Operation(operationId = "deleteRole", summary = "Delete role",
            description = "Fails if the role is still assigned to users. Requires ROLE_MANAGE.")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ApiResponse.ok(messages.get("role.deleted"), null);
    }

    @PutMapping("/{id}/privileges")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.ROLE_MANAGE + "')")
    @Operation(operationId = "updateRolePrivileges", summary = "Replace role privileges",
            description = "Sets the privilege set for a role (many-to-many). Requires ROLE_MANAGE. Privilege changes apply on next access-token issue/refresh.")
    public ApiResponse<RoleResponse> updatePrivileges(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolePrivilegesRequest request) {
        return ApiResponse.ok(messages.get("role.privileges.updated"), roleService.updatePrivileges(id, request));
    }
}
