package com.stockyourlot.repository;

import com.stockyourlot.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommissionRuleRepository extends JpaRepository<CommissionRule, UUID> {
}
