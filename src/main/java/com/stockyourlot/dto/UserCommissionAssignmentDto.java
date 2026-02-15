package com.stockyourlot.dto;

import com.stockyourlot.entity.CommissionType;
import com.stockyourlot.entity.UserCommissionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * User commission assignment in API responses (rule details + start/end, level, status).
 */
public record UserCommissionAssignmentDto(
        UUID ruleId,
        BigDecimal ruleAmount,
        CommissionType ruleCommissionType,
        LocalDate startDate,
        LocalDate endDate,
        int level,
        Integer numberOfSales,
        UserCommissionStatus status
) {}
