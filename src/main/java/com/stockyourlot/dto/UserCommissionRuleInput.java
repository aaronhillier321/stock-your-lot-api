package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * One user commission assignment: link a user to a rule with start/end and optional cap.
 * Used when creating or updating a user (optional list).
 */
public record UserCommissionRuleInput(
        @NotNull(message = "Rule ID is required")
        UUID ruleId,

        @NotNull(message = "Start date is required")
        @JsonAlias("start_date")
        LocalDate startDate,

        @JsonAlias("end_date")
        LocalDate endDate,

        @JsonAlias("level")
        Integer level,

        @JsonAlias("number_of_sales")
        Integer numberOfSales
) {
    public int levelOrDefault() {
        return level != null && level >= 0 ? level : 1;
    }
}
