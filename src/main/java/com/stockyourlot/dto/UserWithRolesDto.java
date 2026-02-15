package com.stockyourlot.dto;

import java.util.List;
import java.util.UUID;

/**
 * User info with global roles, dealership memberships, and commission rule assignments.
 * getUser: userCommissionRules = all assignments; getAllUsers: userCommissionRules = active only.
 */
public record UserWithRolesDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        List<String> roles,
        List<DealershipRoleDto> dealershipRoles,
        List<UserCommissionAssignmentDto> userCommissionRules
) {}
