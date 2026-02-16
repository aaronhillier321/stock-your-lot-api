package com.stockyourlot.repository;

import com.stockyourlot.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    List<Purchase> findByDealership_IdOrderByCreatedAtDesc(UUID dealershipId);

    List<Purchase> findByPurchaseDateBetween(LocalDate startInclusive, LocalDate endInclusive, Sort sort);

    List<Purchase> findByPurchaseDateGreaterThanEqual(LocalDate startInclusive, Sort sort);

    List<Purchase> findByPurchaseDateLessThanEqual(LocalDate endInclusive, Sort sort);

    long countByDealership_Id(UUID dealershipId);

    long countByBuyer_IdAndPurchaseDateBetween(UUID buyerId, LocalDate startInclusive, LocalDate endInclusive);

    long countByDealership_IdAndPurchaseDateBetween(UUID dealershipId, LocalDate startInclusive, LocalDate endInclusive);

    @Query("SELECT p.dealership.id, COUNT(p) FROM Purchase p WHERE p.dealership.id IN :ids GROUP BY p.dealership.id")
    List<Object[]> countByDealershipIds(@Param("ids") List<UUID> ids);
}
