package com.stockyourlot.repository;

import com.stockyourlot.entity.UserCommission;
import com.stockyourlot.entity.UserCommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCommissionRepository extends JpaRepository<UserCommission, UUID> {

    Optional<UserCommission> findByUser_IdAndId(UUID userId, UUID id);

    /**
     * Active assignments for a user, ordered by level descending (highest first).
     * Used to get effective rule and to process expiry on purchase.
     */
    List<UserCommission> findByUser_IdAndStatusOrderByLevelDesc(UUID userId, UserCommissionStatus status);

    boolean existsByUser_IdAndStatusAndLevel(UUID userId, UserCommissionStatus status, int level);

    /**
     * Same as above but with rule eagerly loaded (for recordCommissionsForPurchase).
     */
    @Query("SELECT uc FROM UserCommission uc JOIN FETCH uc.rule WHERE uc.user.id = :userId AND uc.status = :status ORDER BY uc.level DESC")
    List<UserCommission> findByUser_IdAndStatusWithRuleOrderByLevelDesc(
            @Param("userId") UUID userId,
            @Param("status") UserCommissionStatus status);

    /**
     * All assignments for a user with rule fetched (for getUser response).
     */
    @Query("SELECT uc FROM UserCommission uc JOIN FETCH uc.rule WHERE uc.user.id = :userId ORDER BY uc.level DESC")
    List<UserCommission> findByUser_IdWithRuleOrderByLevelDesc(@Param("userId") UUID userId);

    /**
     * Active assignments for multiple users with rule fetched (for getAllUsers response).
     */
    @Query("SELECT uc FROM UserCommission uc JOIN FETCH uc.rule WHERE uc.user.id IN :userIds AND uc.status = :status ORDER BY uc.level DESC")
    List<UserCommission> findByUser_IdInAndStatusWithRuleOrderByLevelDesc(
            @Param("userIds") Collection<UUID> userIds,
            @Param("status") UserCommissionStatus status);

    void deleteByUser_Id(UUID userId);
}
