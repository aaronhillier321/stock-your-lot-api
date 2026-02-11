package com.stockyourlot.dto;

import java.util.UUID;

/**
 * A user's role at a specific dealership (from dealership_users).
 */
public record DealershipRoleDto(
        UUID dealershipId,
        String dealershipName,
        String role
) {}
