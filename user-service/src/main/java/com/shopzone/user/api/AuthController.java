package com.shopzone.user.api;

import com.shopzone.user.api.dto.ForgotPasswordRequest;
import com.shopzone.user.api.dto.LoginRequest;
import com.shopzone.user.api.dto.LogoutRequest;
import com.shopzone.user.api.dto.RefreshRequest;
import com.shopzone.user.api.dto.RegisterRequest;
import com.shopzone.user.api.dto.ResetPasswordRequest;
import com.shopzone.user.api.dto.TokenResponse;
import com.shopzone.user.auth.AuthService;
import com.shopzone.user.web.ApiMessages;
import com.shopzone.user.web.ApiResponse;
import com.shopzone.user.web.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, refresh, logout, and password reset")
@StandardApiErrors
public class AuthController {

    private final AuthService authService;
    private final ApiMessages messages;

    public AuthController(AuthService authService, ApiMessages messages) {
        this.authService = authService;
        this.messages = messages;
    }

    @PostMapping("/register")
    @Operation(operationId = "register", summary = "Register a customer",
            description = "Creates a CUSTOMER account, hashes the password with BCrypt, issues HS256 access JWT and opaque refresh token, writes audit.")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(messages.get("auth.register.success"), data));
    }

    @PostMapping("/login")
    @Operation(operationId = "login", summary = "Login",
            description = "Validates email/password. Returns access JWT (15m, HS256) and refresh token. Same error for unknown email or wrong password.")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(messages.get("auth.login.success"), authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(operationId = "refreshTokens", summary = "Rotate refresh token",
            description = "Exchanges a valid refresh token for a new access JWT and a new refresh token in the same family. Reuse of a revoked token kills the family.")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(messages.get("auth.refresh.success"), authService.refresh(request));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(operationId = "logout", summary = "Logout",
            description = "Revokes the refresh-token family. Access JWT remains valid until expiry.")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.ok(messages.get("auth.logout.success"), null);
    }

    @PostMapping("/forgot-password")
    @Operation(operationId = "forgotPassword", summary = "Request password reset",
            description = "Always returns success. If the email exists, a hashed reset token is stored and the reset URL is logged (demo; no email).")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(messages.get("auth.forgot.success"), null);
    }

    @PostMapping("/reset-password")
    @Operation(operationId = "resetPassword", summary = "Reset password",
            description = "Consumes a one-time reset token (30m TTL), sets a new BCrypt hash, and revokes all refresh families for the user.")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(messages.get("auth.reset.success"), null);
    }
}
