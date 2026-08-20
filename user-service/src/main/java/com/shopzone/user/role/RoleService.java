package com.shopzone.user.role;

import com.shopzone.user.api.dto.CreateRoleRequest;
import com.shopzone.user.api.dto.Mappers;
import com.shopzone.user.api.dto.RoleResponse;
import com.shopzone.user.api.dto.UpdateRolePrivilegesRequest;
import com.shopzone.user.api.dto.UpdateRoleRequest;
import com.shopzone.user.audit.AuditService;
import com.shopzone.user.domain.Privilege;
import com.shopzone.user.domain.Role;
import com.shopzone.user.repository.PrivilegeRepository;
import com.shopzone.user.repository.RoleRepository;
import com.shopzone.user.web.BusinessException;
import com.shopzone.user.web.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final AuditService auditService;

    public RoleService(
            RoleRepository roleRepository,
            PrivilegeRepository privilegeRepository,
            AuditService auditService) {
        this.roleRepository = roleRepository;
        this.privilegeRepository = privilegeRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAllWithPrivileges().stream().map(Mappers::toRole).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse get(UUID id) {
        return Mappers.toRole(requireRole(id));
    }

    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.conflict("role.name.exists");
        }
        Role role = new Role();
        role.setName(request.name().trim());
        role.setDescription(request.description() == null ? "" : request.description().trim());
        role = roleRepository.save(role);
        auditService.record(SecurityUtils.currentUserId(), "ROLE_CREATED", "ROLE", role.getId(),
                Map.of("name", role.getName()));
        return Mappers.toRole(requireRole(role.getId()));
    }

    public RoleResponse update(UUID id, UpdateRoleRequest request) {
        Role role = requireRole(id);
        if (roleRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw BusinessException.conflict("role.name.exists");
        }
        role.setName(request.name().trim());
        role.setDescription(request.description() == null ? "" : request.description().trim());
        auditService.record(SecurityUtils.currentUserId(), "ROLE_UPDATED", "ROLE", id, Map.of());
        return Mappers.toRole(role);
    }

    public void delete(UUID id) {
        Role role = requireRole(id);
        if (roleRepository.countActiveUsersByRoleId(id) > 0) {
            throw BusinessException.conflict("role.cannot.delete.assigned");
        }
        roleRepository.delete(role);
        auditService.record(SecurityUtils.currentUserId(), "ROLE_DELETED", "ROLE", id, Map.of("name", role.getName()));
    }

    public RoleResponse updatePrivileges(UUID id, UpdateRolePrivilegesRequest request) {
        Role role = requireRole(id);
        List<Privilege> privileges = privilegeRepository.findAllByIdIn(request.privilegeIds());
        if (privileges.size() != request.privilegeIds().size()) {
            throw BusinessException.notFound("role.privilege.not.found");
        }
        role.setPrivileges(new HashSet<>(privileges));
        auditService.record(SecurityUtils.currentUserId(), "ROLE_PRIVILEGES_UPDATED", "ROLE", id, Map.of());
        return Mappers.toRole(role);
    }

    private Role requireRole(UUID id) {
        return roleRepository.findWithPrivilegesById(id)
                .orElseThrow(() -> BusinessException.notFound("role.not.found"));
    }
}
