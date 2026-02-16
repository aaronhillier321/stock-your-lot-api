package com.stockyourlot.dto;

import java.util.UUID;

/**
 * User membership at a dealership (for getUsersByDealer response).
 */
public record UserAtDealershipDto(
        UUID userId,
        String username,
        String email,
        String role
) {}
