package com.stockyourlot.repository;

import com.stockyourlot.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    List<Purchase> findByDealership_IdOrderByCreatedAtDesc(UUID dealershipId);

    long countByDealership_Id(UUID dealershipId);

    @Query("SELECT p.dealership.id, COUNT(p) FROM Purchase p WHERE p.dealership.id IN :ids GROUP BY p.dealership.id")
    List<Object[]> countByDealershipIds(@Param("ids") List<UUID> ids);
}
