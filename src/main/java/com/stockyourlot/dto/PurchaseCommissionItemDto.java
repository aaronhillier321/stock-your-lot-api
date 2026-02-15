package com.stockyourlot.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One commission line for a purchase (user credited, rule used, amount).
 */
public record PurchaseCommissionItemDto(
        UUID userId,
        String userUsername,
        UUID ruleId,
        BigDecimal amount
) {}
