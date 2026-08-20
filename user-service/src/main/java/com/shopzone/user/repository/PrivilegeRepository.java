package com.shopzone.user.repository;

import com.shopzone.user.domain.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {

    Optional<Privilege> findByName(String name);

    List<Privilege> findAllByIdIn(Collection<UUID> ids);

    List<Privilege> findAllByOrderByNameAsc();
}
