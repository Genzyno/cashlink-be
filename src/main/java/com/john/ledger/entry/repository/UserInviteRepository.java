package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.UserInviteEntity;
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
public interface UserInviteRepository extends JpaRepository<UserInviteEntity, UUID> {

    Optional<UserInviteEntity> findByToken(String token);

    Optional<UserInviteEntity> findByEmailAndStatus(String email, UserInviteEntity.InviteStatus status);

    boolean existsByEmailAndStatus(String email, UserInviteEntity.InviteStatus status);

    /** Accepted/rejected invites sent by the given user, optionally filtered by business (via invite's books). */
    @Query("""
        SELECT i FROM UserInviteEntity i
        WHERE i.invitedByUserId = :invitedByUserId AND i.status IN :statuses
        AND (:businessId IS NULL OR i.id IN (
            SELECT ub.inviteId FROM UserInviteBookEntity ub, BookEntity b
            WHERE ub.bookId = b.id AND b.businessId = :businessId
        ))
        """)
    Page<UserInviteEntity> findByInvitedByUserIdAndStatusInAndOptionalBusiness(
            @Param("invitedByUserId") UUID invitedByUserId,
            @Param("statuses") List<UserInviteEntity.InviteStatus> statuses,
            @Param("businessId") UUID businessId,
            Pageable pageable);
}
