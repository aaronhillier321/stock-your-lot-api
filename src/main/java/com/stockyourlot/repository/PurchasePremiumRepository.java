package com.stockyourlot.repository;

import com.stockyourlot.entity.PurchasePremium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchasePremiumRepository extends JpaRepository<PurchasePremium, UUID> {

    List<PurchasePremium> findByPurchase_IdOrderByCreatedAtAsc(UUID purchaseId);
}
