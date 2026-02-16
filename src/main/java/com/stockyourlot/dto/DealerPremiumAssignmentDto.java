package com.stockyourlot.dto;

import com.stockyourlot.entity.DealerPremiumStatus;
import com.stockyourlot.entity.PremiumType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dealer premium assignment in API responses (rule details + start/end, level, status).
 */
public record DealerPremiumAssignmentDto(
        UUID id,
        UUID ruleId,
        BigDecimal ruleAmount,
        PremiumType rulePremiumType,
        LocalDate startDate,
        LocalDate endDate,
        int level,
        Integer numberOfSales,
        DealerPremiumStatus status
) {}
