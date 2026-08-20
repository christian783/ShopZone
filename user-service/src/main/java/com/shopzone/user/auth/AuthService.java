package com.shopzone.user.auth;

import com.shopzone.user.api.dto.ForgotPasswordRequest;
import com.shopzone.user.api.dto.LoginRequest;
import com.shopzone.user.api.dto.LogoutRequest;
import com.shopzone.user.api.dto.RefreshRequest;
import com.shopzone.user.api.dto.RegisterRequest;
import com.shopzone.user.api.dto.ResetPasswordRequest;
import com.shopzone.user.api.dto.TokenResponse;
import com.shopzone.user.api.dto.UpdateProfileRequest;
import com.shopzone.user.api.dto.UserResponse;

public interface AuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    UserResponse me();

    UserResponse updateMe(UpdateProfileRequest request);
}
