package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request to add a commission assignment for a user (by user id).
 */
public record AddUserCommissionRequest(
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
