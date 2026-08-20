package com.shopzone.user.repository;

import com.shopzone.user.domain.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @EntityGraph(attributePaths = "privileges")
    Optional<Role> findWithPrivilegesById(UUID id);

    @EntityGraph(attributePaths = "privileges")
    List<Role> findAllByIdIn(Collection<UUID> ids);

    @EntityGraph(attributePaths = "privileges")
    @Query("select r from Role r order by r.name")
    List<Role> findAllWithPrivileges();

    @Query("select count(u) from User u join u.roles r where r.id = :roleId and u.deletedAt is null")
    long countActiveUsersByRoleId(UUID roleId);
}
