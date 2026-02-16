package com.stockyourlot.repository;

import com.stockyourlot.entity.PurchaseCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PurchaseCommissionRepository extends JpaRepository<PurchaseCommission, UUID> {

    List<PurchaseCommission> findByPurchase_IdOrderByCreatedAtAsc(UUID purchaseId);

    @Query("SELECT pc FROM PurchaseCommission pc JOIN FETCH pc.user JOIN FETCH pc.rule JOIN FETCH pc.purchase WHERE pc.purchase.id IN :purchaseIds ORDER BY pc.purchase.id, pc.createdAt")
    List<PurchaseCommission> findByPurchase_IdInWithUserAndRuleOrderByCreatedAtAsc(@Param("purchaseIds") List<UUID> purchaseIds);

    List<PurchaseCommission> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
