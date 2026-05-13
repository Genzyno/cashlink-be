package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByRoleName(String roleName);

    /** All roles paginated, scoped to admin, Super Admin first then by role name. */
    @Query("SELECT r FROM RoleEntity r WHERE r.adminId IS NULL OR r.adminId = :adminId ORDER BY CASE WHEN r.roleName = 'Super Admin' THEN 0 ELSE 1 END ASC, r.roleName ASC")
    Page<RoleEntity> findAllByAdminId(UUID adminId, Pageable pageable);

    Optional<RoleEntity> findByRoleNameAndAdminId(String roleName, UUID adminId);
}
