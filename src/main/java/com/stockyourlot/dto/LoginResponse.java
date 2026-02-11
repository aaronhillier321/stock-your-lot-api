package com.stockyourlot.dto;

import java.util.List;

public record LoginResponse(
        String message,
        String username,
        String email,
        List<String> roles,
        String token
) {}
