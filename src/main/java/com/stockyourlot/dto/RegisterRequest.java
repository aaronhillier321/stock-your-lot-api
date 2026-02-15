package com.stockyourlot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Role is required (BUYER, DEALER, or ADMIN)")
        @Size(max = 20)
        String role,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 50)
        String phoneNumber,

        /** Optional commission rule assignments for the new user (ruleId, startDate, endDate?, level?, numberOfSales?). */
        @Valid
        List<UserCommissionRuleInput> userCommissionRules
) {}
