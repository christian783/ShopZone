package com.shopzone.user.repository;

import com.shopzone.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
    Optional<User> findWithRolesById(UUID id);

    @EntityGraph(attributePaths = {"roles", "roles.privileges"})
    Optional<User> findWithRolesByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNullAndIdNot(String email, UUID id);

    @Query("""
            select count(u) from User u
            join u.roles r
            where r.name = :roleName and u.deletedAt is null and u.enabled = true
            """)
    long countActiveByRoleName(String roleName);
}
