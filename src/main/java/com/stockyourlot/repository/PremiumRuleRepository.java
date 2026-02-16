package com.stockyourlot.repository;

import com.stockyourlot.entity.PremiumRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PremiumRuleRepository extends JpaRepository<PremiumRule, UUID> {
}
