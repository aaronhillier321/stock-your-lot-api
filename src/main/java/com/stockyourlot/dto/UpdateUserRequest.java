package com.stockyourlot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating a user. All fields optional; only provided fields are updated.
 */
public record UpdateUserRequest(
        @Size(min = 2, max = 100)
        String username,

        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 50)
        String phoneNumber
) {}
