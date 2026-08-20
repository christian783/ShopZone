package com.shopzone.user.api.dto;

import com.shopzone.user.domain.Privilege;
import com.shopzone.user.domain.Role;
import com.shopzone.user.domain.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class Mappers {

    private Mappers() {
    }

    public static UserResponse toUser(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                roles,
                user.privilegeNames());
    }

    public static RoleResponse toRole(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPrivileges().stream().map(Mappers::toPrivilege).collect(Collectors.toSet()));
    }

    public static PrivilegeResponse toPrivilege(Privilege privilege) {
        return new PrivilegeResponse(privilege.getId(), privilege.getName(), privilege.getDescription());
    }
}
