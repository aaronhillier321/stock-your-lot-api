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
        List<String> roles,
        List<DealershipRoleDto> dealershipRoles
) {}
