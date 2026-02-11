package com.stockyourlot.dto;

import java.util.List;
import java.util.UUID;

public record UserRolesResponse(
        UUID userId,
        String username,
        String email,
        List<String> roles
) {}
