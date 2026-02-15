package com.stockyourlot.dto;

import com.stockyourlot.entity.CommissionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCommissionRuleRequest(
        @NotBlank(message = "Rule name is required")
        @Size(max = 100)
        String ruleName,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0", inclusive = true, message = "Amount must be non-negative")
        BigDecimal amount,

        @NotNull(message = "Commission type is required (PERCENT or FLAT)")
        CommissionType commissionType
) {}
