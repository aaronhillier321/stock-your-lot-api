package com.stockyourlot.repository;

import com.stockyourlot.entity.DealerPremium;
import com.stockyourlot.entity.DealerPremiumStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealerPremiumRepository extends JpaRepository<DealerPremium, UUID> {

    Optional<DealerPremium> findByDealership_IdAndId(UUID dealershipId, UUID id);

    boolean existsByDealership_IdAndStatusAndLevel(UUID dealershipId, DealerPremiumStatus status, int level);

    List<DealerPremium> findByDealership_IdAndStatusOrderByLevelDesc(UUID dealershipId, DealerPremiumStatus status);

    @Query("SELECT dp FROM DealerPremium dp JOIN FETCH dp.rule WHERE dp.dealership.id = :dealershipId AND dp.status = :status ORDER BY dp.level DESC")
    List<DealerPremium> findByDealership_IdAndStatusWithRuleOrderByLevelDesc(
            @Param("dealershipId") UUID dealershipId,
            @Param("status") DealerPremiumStatus status);

    @Query("SELECT dp FROM DealerPremium dp JOIN FETCH dp.rule WHERE dp.dealership.id = :dealershipId ORDER BY dp.level DESC")
    List<DealerPremium> findByDealership_IdWithRuleOrderByLevelDesc(@Param("dealershipId") UUID dealershipId);

    @Query("SELECT dp FROM DealerPremium dp JOIN FETCH dp.rule WHERE dp.dealership.id IN :dealershipIds AND dp.status = :status ORDER BY dp.level DESC")
    List<DealerPremium> findByDealership_IdInAndStatusWithRuleOrderByLevelDesc(
            @Param("dealershipIds") Collection<UUID> dealershipIds,
            @Param("status") DealerPremiumStatus status);

    void deleteByDealership_Id(UUID dealershipId);
}
