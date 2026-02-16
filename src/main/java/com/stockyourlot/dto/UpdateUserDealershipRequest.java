package com.stockyourlot.dto;

import jakarta.validation.constraints.Size;

/** Request to update a user's role at a dealership. */
public record UpdateUserDealershipRequest(
        /** BUYER or ADMIN. */
        @Size(max = 20)
        String role
) {}
