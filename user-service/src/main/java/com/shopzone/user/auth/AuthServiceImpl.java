package com.shopzone.user.auth;

import com.shopzone.user.api.dto.ForgotPasswordRequest;
import com.shopzone.user.api.dto.LoginRequest;
import com.shopzone.user.api.dto.LogoutRequest;
import com.shopzone.user.api.dto.Mappers;
import com.shopzone.user.api.dto.RefreshRequest;
import com.shopzone.user.api.dto.RegisterRequest;
import com.shopzone.user.api.dto.ResetPasswordRequest;
import com.shopzone.user.api.dto.TokenResponse;
import com.shopzone.user.api.dto.UpdateProfileRequest;
import com.shopzone.user.api.dto.UserResponse;
import com.shopzone.user.audit.AuditService;
import com.shopzone.user.config.JwtProperties;
import com.shopzone.user.domain.PasswordResetToken;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.domain.Role;
import com.shopzone.user.domain.User;
import com.shopzone.user.repository.PasswordResetTokenRepository;
import com.shopzone.user.repository.RoleRepository;
import com.shopzone.user.repository.UserRepository;
import com.shopzone.user.web.BusinessException;
import com.shopzone.user.web.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AuditService auditService;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            JwtProperties jwtProperties,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
        this.auditService = auditService;
    }

    @Override
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw BusinessException.conflict("user.email.exists");
        }
        Role customer = roleRepository.findByName(PrivilegeNames.CUSTOMER)
                .orElseThrow(() -> BusinessException.notFound("role.not.found"));
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.getRoles().add(customer);
        user = userRepository.save(user);
        user = userRepository.findWithRolesById(user.getId()).orElse(user);
        auditService.record(user.getId(), "USER_REGISTERED", "USER", user.getId(), Map.of("email", user.getEmail()));
        return issueTokens(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findWithRolesByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> BusinessException.unauthorized("auth.invalid.credentials"));
        if (!user.isEnabled()) {
            throw BusinessException.unauthorized("auth.account.disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("auth.invalid.credentials");
        }
        auditService.record(user.getId(), "USER_LOGIN", "USER", user.getId(), Map.of());
        return issueTokens(user);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(request.refreshToken());
        User user = userRepository.findWithRolesById(result.user().getId())
                .orElseThrow(() -> BusinessException.unauthorized("auth.refresh.invalid"));
        if (!user.isEnabled() || user.isDeleted()) {
            throw BusinessException.unauthorized("auth.account.disabled");
        }
        return toTokenResponse(user, result.refresh().plaintext());
    }

    @Override
    public void logout(LogoutRequest request) {
        refreshTokenService.revokeFamilyByToken(request.refreshToken());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findWithRolesByEmailIgnoreCaseAndDeletedAtIsNull(request.email()).ifPresent(user -> {
            String plaintext = TokenHasher.randomToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(TokenHasher.sha256(plaintext));
            token.setExpiresAt(Instant.now().plus(jwtProperties.getResetTokenTtl()));
            passwordResetTokenRepository.save(token);
            log.info("Password reset URL (demo only): http://localhost:8080/reset-password?token={}", plaintext);
            auditService.record(user.getId(), "PASSWORD_RESET_REQUESTED", "USER", user.getId(), Map.of());
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256(request.token()))
                .orElseThrow(() -> BusinessException.badRequest("auth.reset.invalid"));
        if (!token.isUsable(Instant.now())) {
            throw BusinessException.badRequest("auth.reset.invalid");
        }
        User user = userRepository.findWithRolesById(token.getUser().getId())
                .orElseThrow(() -> BusinessException.badRequest("auth.reset.invalid"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        token.setUsedAt(Instant.now());
        refreshTokenService.revokeAllForUser(user.getId());
        auditService.record(user.getId(), "PASSWORD_RESET", "USER", user.getId(), Map.of());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me() {
        return Mappers.toUser(loadCurrent());
    }

    @Override
    public UserResponse updateMe(UpdateProfileRequest request) {
        User user = loadCurrent();
        user.setFullName(request.fullName().trim());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            refreshTokenService.revokeAllForUser(user.getId());
        }
        auditService.record(user.getId(), "USER_UPDATED", "USER", user.getId(), Map.of("self", true));
        return Mappers.toUser(user);
    }

    private User loadCurrent() {
        return userRepository.findWithRolesById(SecurityUtils.currentUserId())
                .orElseThrow(() -> BusinessException.notFound("user.not.found"));
    }

    private TokenResponse issueTokens(User user) {
        RefreshTokenService.IssuedRefresh refresh = refreshTokenService.issue(user);
        return toTokenResponse(user, refresh.plaintext());
    }

    private TokenResponse toTokenResponse(User user, String refreshPlaintext) {
        return new TokenResponse(
                jwtService.createAccessToken(user),
                refreshPlaintext,
                "Bearer",
                jwtService.accessTokenTtlSeconds());
    }
}
