package com.stockyourlot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddDealershipUserRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,
        @NotNull(message = "dealershipId must not be null")
        UUID dealershipId,
        /** Optional. ASSOCIATE or ADMIN; defaults to ASSOCIATE if null or blank. */
        String role
) {}
