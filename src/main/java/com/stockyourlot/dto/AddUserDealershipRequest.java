package com.stockyourlot.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Request to add a user to a dealership (by user id). */
public record AddUserDealershipRequest(
        @NotNull(message = "dealershipId must not be null")
        UUID dealershipId,
        /** Optional. BUYER or ADMIN; defaults to BUYER if null or blank. */
        String role
) {}
