package com.stockyourlot.dto;

import com.stockyourlot.entity.CommissionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CommissionRuleResponse(
        UUID id,
        String ruleName,
        BigDecimal amount,
        CommissionType commissionType
) {}
