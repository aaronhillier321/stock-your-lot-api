package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body for updating a user. All fields optional; only provided fields are updated.
 * When {@code roles} is provided, the user's global roles are replaced exactly with the given list
 * (roles not in the list are removed). Accepts camelCase or snake_case in JSON.
 */
public record UpdateUserRequest(
        @Size(min = 2, max = 100)
        String username,

        @Email(message = "Email must be valid")
        @Size(max = 255)
        String email,

        @Size(max = 100)
        @JsonAlias("first_name")
        String firstName,

        @Size(max = 100)
        @JsonAlias("last_name")
        String lastName,

        @Size(max = 50)
        @JsonAlias("phone_number")
        String phoneNumber,

        /** Global role names (BUYER, DEALER, ADMIN). When provided, replaces user's roles with this set. */
        List<String> roles
) {}
