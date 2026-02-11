package com.stockyourlot.repository;

import com.stockyourlot.entity.DealershipUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DealershipUserRepository extends JpaRepository<DealershipUser, UUID> {

    Optional<DealershipUser> findByUser_IdAndDealership_Id(UUID userId, UUID dealershipId);
}
