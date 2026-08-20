package com.shopzone.user.privilege;

import com.shopzone.user.api.dto.Mappers;
import com.shopzone.user.api.dto.PrivilegeResponse;
import com.shopzone.user.repository.PrivilegeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;

    public PrivilegeService(PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    @Transactional(readOnly = true)
    public List<PrivilegeResponse> list() {
        return privilegeRepository.findAllByOrderByNameAsc().stream().map(Mappers::toPrivilege).toList();
    }
}
