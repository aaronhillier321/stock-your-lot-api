package com.stockyourlot.dto;

import com.stockyourlot.entity.PremiumType;

import java.math.BigDecimal;
import java.util.UUID;

public record PremiumRuleResponse(
        UUID id,
        String ruleName,
        BigDecimal amount,
        PremiumType premiumType
) {}
