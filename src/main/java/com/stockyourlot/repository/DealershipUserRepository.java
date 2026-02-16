package com.stockyourlot.repository;

import com.stockyourlot.entity.DealershipUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealershipUserRepository extends JpaRepository<DealershipUser, UUID> {

    Optional<DealershipUser> findByUser_IdAndDealership_Id(UUID userId, UUID dealershipId);

    void deleteByUser_IdAndDealership_Id(UUID userId, UUID dealershipId);

    @Query("SELECT du FROM DealershipUser du JOIN FETCH du.user WHERE du.dealership.id = :dealershipId")
    List<DealershipUser> findByDealership_IdWithUser(@Param("dealershipId") UUID dealershipId);
}
