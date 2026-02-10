package com.stockyourlot.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String username,
        String email,
        String message
) {}
