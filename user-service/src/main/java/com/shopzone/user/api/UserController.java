package com.shopzone.user.api;

import com.shopzone.user.api.dto.CreateUserRequest;
import com.shopzone.user.api.dto.UpdateProfileRequest;
import com.shopzone.user.api.dto.UpdateUserRequest;
import com.shopzone.user.api.dto.UserResponse;
import com.shopzone.user.auth.AuthService;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.user.UserAdminService;
import com.shopzone.user.web.ApiMessages;
import com.shopzone.user.web.ApiResponse;
import com.shopzone.user.web.PageResponse;
import com.shopzone.user.web.SecurityUtils;
import com.shopzone.user.web.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Admin user management (soft delete)")
@SecurityRequirement(name = "bearer-jwt")
@StandardApiErrors
public class UserController {

    private final UserAdminService userAdminService;
    private final AuthService authService;
    private final ApiMessages messages;

    public UserController(UserAdminService userAdminService, AuthService authService, ApiMessages messages) {
        this.userAdminService = userAdminService;
        this.authService = authService;
        this.messages = messages;
    }

    @GetMapping("/me")
    @Operation(operationId = "currentUser", summary = "Current profile",
            description = "Returns the authenticated user. No special privilege required.")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(messages.get("user.me"), authService.me());
    }

    @PatchMapping("/me")
    @Operation(operationId = "updateCurrentUser", summary = "Update current profile",
            description = "Updates full name and optional password. Changing password revokes all refresh tokens.")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(messages.get("user.me.updated"), authService.updateMe(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.USER_READ + "')")
    @Operation(operationId = "listUsers", summary = "List users",
            description = "Paginated list of non-deleted users. Requires USER_READ.")
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ApiResponse.ok(messages.get("user.list"),
                userAdminService.list(email, enabled, SecurityUtils.pageable(page, size, sort)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.USER_READ + "')")
    @Operation(operationId = "getUser", summary = "Get user", description = "Requires USER_READ. 404 if missing or soft-deleted.")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(messages.get("user.get"), userAdminService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PrivilegeNames.USER_CREATE + "')")
    @Operation(operationId = "createUser", summary = "Create user",
            description = "Admin create with explicit role IDs. Password is BCrypt-hashed. Requires USER_CREATE. Audited.")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(messages.get("user.created"), userAdminService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.USER_UPDATE + "')")
    @Operation(operationId = "updateUser", summary = "Update user",
            description = "Updates email, name, enabled flag, roles, optional password. Requires USER_UPDATE. Audited.")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(messages.get("user.updated"), userAdminService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PrivilegeNames.USER_DELETE + "')")
    @Operation(operationId = "deleteUser", summary = "Soft-delete user",
            description = "Sets deletedAt; revokes refresh tokens. Cannot delete self or the last enabled admin. Requires USER_DELETE.")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        userAdminService.delete(id);
        return ApiResponse.ok(messages.get("user.deleted"), null);
    }
}
