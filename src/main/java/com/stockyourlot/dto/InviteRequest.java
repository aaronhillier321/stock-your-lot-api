package com.stockyourlot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Invite request. Email is required; dealershipId is optional (invites can be global).
 * Other user fields are optional and used to pre-populate the pending user (and for display on the client).
 */
public record InviteRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,

        UUID dealershipId,

        String firstName,
        String lastName,
        String phoneNumber,
        String role
) {}
