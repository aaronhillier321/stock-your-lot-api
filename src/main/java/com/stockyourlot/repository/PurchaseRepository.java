package com.stockyourlot.repository;

import com.stockyourlot.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
}
