package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalDate;

/**
 * Request to update a user commission assignment (partial update).
 */
public record UpdateUserCommissionRequest(
        @JsonAlias("start_date")
        LocalDate startDate,

        @JsonAlias("end_date")
        LocalDate endDate,

        @JsonAlias("level")
        Integer level,

        @JsonAlias("number_of_sales")
        Integer numberOfSales
) {}
