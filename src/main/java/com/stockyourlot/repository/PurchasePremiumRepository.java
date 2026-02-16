package com.stockyourlot.repository;

import com.stockyourlot.entity.PurchasePremium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PurchasePremiumRepository extends JpaRepository<PurchasePremium, UUID> {

    List<PurchasePremium> findByPurchase_IdOrderByCreatedAtAsc(UUID purchaseId);

    @Query("SELECT COALESCE(SUM(pp.amount), 0) FROM PurchasePremium pp WHERE pp.dealership.id = :dealershipId")
    BigDecimal sumAmountByDealership_Id(@Param("dealershipId") UUID dealershipId);

    @Query("SELECT pp.purchase.id, SUM(pp.amount) FROM PurchasePremium pp WHERE pp.purchase.id IN :ids GROUP BY pp.purchase.id")
    List<Object[]> sumAmountByPurchaseIds(@Param("ids") List<UUID> ids);
}
