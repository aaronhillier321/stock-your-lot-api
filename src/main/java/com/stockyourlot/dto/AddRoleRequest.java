package com.stockyourlot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request to add a global role (USER, SALES_ASSOCIATE, SALES_ADMIN) to a user.
 */
public record AddRoleRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,
        @NotBlank(message = "role must not be blank")
        String role
) {}
