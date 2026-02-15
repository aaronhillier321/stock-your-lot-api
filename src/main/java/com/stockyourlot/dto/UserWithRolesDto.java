package com.stockyourlot.dto;

import java.util.List;
import java.util.UUID;

/**
 * User info with global roles and dealership memberships (for list/get-all responses).
 */
public record UserWithRolesDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        List<String> roles,
        List<DealershipRoleDto> dealershipRoles
) {}
