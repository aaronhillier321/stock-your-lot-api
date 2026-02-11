package com.stockyourlot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetUserRolesRequest(
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be valid")
        String email,
        @NotNull(message = "roles must not be null")
        List<String> roles
) {}
