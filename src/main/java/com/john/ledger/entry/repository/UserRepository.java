package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUserName(String userName);

    Optional<UserEntity> findByUserEmail(String userEmail);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roleEntity WHERE u.userEmail = :email")
    Optional<UserEntity> findByUserEmailWithRole(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roleEntity WHERE u.id = :id")
    Optional<UserEntity> findByIdWithRole(@Param("id") java.util.UUID id);

    Optional<UserEntity> findByUserMobile(String userMobile);

    @Query("""
        SELECT u FROM UserEntity u
        LEFT JOIN u.roleEntity r
        WHERE
        LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))
        OR u.userMobile LIKE CONCAT('%', :search, '%')
        OR (r.roleName IS NOT NULL AND LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<UserEntity> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r WHERE u.id IN :ids")
    Page<UserEntity> findByIdIn(@Param("ids") List<UUID> ids, Pageable pageable);

    @Query("SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r WHERE u.adminId = :adminId")
    Page<UserEntity> findByAdminId(@Param("adminId") UUID adminId, Pageable pageable);

    @Query("""
        SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r
        WHERE u.id IN :ids AND (
        LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))
        OR u.userMobile LIKE CONCAT('%', :search, '%')
        OR (r.roleName IS NOT NULL AND LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))
        )
        """)
    Page<UserEntity> findByIdInAndSearch(@Param("ids") List<UUID> ids, @Param("search") String search, Pageable pageable);

    @Query("""
        SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r
        WHERE u.adminId = :adminId AND (
        LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))
        OR u.userMobile LIKE CONCAT('%', :search, '%')
        OR (r.roleName IS NOT NULL AND LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))
        )
        """)
    Page<UserEntity> findByAdminIdAndSearch(@Param("adminId") UUID adminId, @Param("search") String search, Pageable pageable);
    @Query("SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r WHERE u.adminId = :adminId OR u.id = :adminId")
    Page<UserEntity> findByAdminIdOrId(@Param("adminId") UUID adminId, Pageable pageable);

    @Query("""
        SELECT u FROM UserEntity u LEFT JOIN u.roleEntity r
        WHERE (u.adminId = :adminId OR u.id = :adminId) AND (
        LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(u.userEmail) LIKE LOWER(CONCAT('%', :search, '%'))
        OR u.userMobile LIKE CONCAT('%', :search, '%')
        OR (r.roleName IS NOT NULL AND LOWER(r.roleName) LIKE LOWER(CONCAT('%', :search, '%')))
        )
        """)
    Page<UserEntity> findByAdminIdOrIdAndSearch(@Param("adminId") UUID adminId, @Param("search") String search, Pageable pageable);
}
