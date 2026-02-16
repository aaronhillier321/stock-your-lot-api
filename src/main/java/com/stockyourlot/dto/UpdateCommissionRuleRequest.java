package com.stockyourlot.dto;

import com.stockyourlot.entity.CommissionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for updating a commission rule. All fields optional; only provided fields are updated.
 */
public record UpdateCommissionRuleRequest(
        @Size(max = 100)
        String ruleName,

        @DecimalMin(value = "0", inclusive = true, message = "Amount must be non-negative")
        BigDecimal amount,

        CommissionType commissionType
) {}
