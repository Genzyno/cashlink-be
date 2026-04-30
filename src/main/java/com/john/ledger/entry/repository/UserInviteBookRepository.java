package com.john.ledger.entry.repository;

import com.john.ledger.entry.entity.UserInviteBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserInviteBookRepository extends JpaRepository<UserInviteBookEntity, UUID> {

    List<UserInviteBookEntity> findByInviteIdOrderByBookId(UUID inviteId);
}
