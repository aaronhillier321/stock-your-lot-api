package com.stockyourlot.dto;

import java.math.BigDecimal;

/**
 * Summary of what a dealership owes for the current month: purchase count and premium totals.
 */
public record DealershipPremiumSummaryDto(
        long purchasesThisMonth,
        BigDecimal totalPremiums,
        BigDecimal totalInvoice
) {}
