package com.stockyourlot.repository;

import com.stockyourlot.entity.PurchaseCommission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseCommissionRepository extends JpaRepository<PurchaseCommission, UUID> {

    List<PurchaseCommission> findByPurchase_IdOrderByCreatedAtAsc(UUID purchaseId);

    List<PurchaseCommission> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
