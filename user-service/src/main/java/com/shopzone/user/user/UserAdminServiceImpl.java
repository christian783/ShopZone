package com.shopzone.user.user;

import com.shopzone.user.api.dto.CreateUserRequest;
import com.shopzone.user.api.dto.Mappers;
import com.shopzone.user.api.dto.UpdateUserRequest;
import com.shopzone.user.api.dto.UserResponse;
import com.shopzone.user.audit.AuditService;
import com.shopzone.user.auth.RefreshTokenService;
import com.shopzone.user.domain.PrivilegeNames;
import com.shopzone.user.domain.Role;
import com.shopzone.user.domain.User;
import com.shopzone.user.repository.RoleRepository;
import com.shopzone.user.repository.UserRepository;
import com.shopzone.user.repository.UserSpecs;
import com.shopzone.user.web.BusinessException;
import com.shopzone.user.web.PageResponse;
import com.shopzone.user.web.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    public UserAdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String email, Boolean enabled, Pageable pageable) {
        Specification<User> spec = UserSpecs.notDeleted();
        Specification<User> emailSpec = UserSpecs.emailContains(email);
        if (emailSpec != null) {
            spec = spec.and(emailSpec);
        }
        Specification<User> enabledSpec = UserSpecs.enabled(enabled);
        if (enabledSpec != null) {
            spec = spec.and(enabledSpec);
        }
        Page<UserResponse> page = userRepository.findAll(spec, pageable)
                .map(user -> userRepository.findWithRolesById(user.getId()).orElse(user))
                .map(Mappers::toUser);
        return SecurityUtils.toPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return Mappers.toUser(requireActive(id));
    }

    @Override
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.email())) {
            throw BusinessException.conflict("user.email.exists");
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRoles(loadRoles(request.roleIds()));
        user = userRepository.save(user);
        user = requireActive(user.getId());
        auditService.record(SecurityUtils.currentUserId(), "USER_CREATED", "USER", user.getId(),
                Map.of("email", user.getEmail()));
        return Mappers.toUser(user);
    }

    @Override
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = requireActive(id);
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(request.email(), id)) {
            throw BusinessException.conflict("user.email.exists");
        }
        boolean wasAdmin = user.hasRole(PrivilegeNames.ADMIN);
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setEnabled(request.enabled());
        user.setRoles(loadRoles(request.roleIds()));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            refreshTokenService.revokeAllForUser(user.getId());
        }
        if (wasAdmin && !user.hasRole(PrivilegeNames.ADMIN) && !request.enabled()) {
            ensureNotLastAdmin(user.getId());
        }
        if (wasAdmin && !user.hasRole(PrivilegeNames.ADMIN)) {
            ensureNotLastAdmin(user.getId());
        }
        auditService.record(SecurityUtils.currentUserId(), "USER_UPDATED", "USER", user.getId(), Map.of());
        return Mappers.toUser(user);
    }

    @Override
    public void delete(UUID id) {
        UUID actor = SecurityUtils.currentUserId();
        if (actor.equals(id)) {
            throw BusinessException.forbidden("user.cannot.delete.self");
        }
        User user = requireActive(id);
        if (user.hasRole(PrivilegeNames.ADMIN)) {
            ensureNotLastAdmin(id);
        }
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        refreshTokenService.revokeAllForUser(id);
        auditService.record(actor, "USER_DELETED", "USER", id, Map.of("email", user.getEmail()));
    }

    private User requireActive(UUID id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> BusinessException.notFound("user.not.found"));
        if (user.isDeleted()) {
            throw BusinessException.notFound("user.not.found");
        }
        return user;
    }

    private Set<Role> loadRoles(Set<UUID> roleIds) {
        List<Role> roles = roleRepository.findAllByIdIn(roleIds);
        if (roles.size() != roleIds.size()) {
            throw BusinessException.notFound("role.not.found");
        }
        return new HashSet<>(roles);
    }

    private void ensureNotLastAdmin(UUID excludingUserId) {
        long remaining = userRepository.countActiveByRoleName(PrivilegeNames.ADMIN);
        User current = userRepository.findWithRolesById(excludingUserId).orElse(null);
        boolean currentIsActiveAdmin = current != null
                && !current.isDeleted()
                && current.isEnabled()
                && current.hasRole(PrivilegeNames.ADMIN);
        if (currentIsActiveAdmin && remaining <= 1) {
            throw BusinessException.conflict("user.cannot.delete.last.admin");
        }
    }
}
