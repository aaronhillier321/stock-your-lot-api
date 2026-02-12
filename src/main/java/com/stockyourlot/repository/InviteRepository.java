package com.stockyourlot.repository;

import com.stockyourlot.entity.Invite;
import com.stockyourlot.entity.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteRepository extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByTokenHash(String tokenHash);

    List<Invite> findByEmailAndStatus(String email, InviteStatus status);
}
