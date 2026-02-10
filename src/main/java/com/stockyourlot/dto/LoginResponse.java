package com.stockyourlot.dto;

public record LoginResponse(
        String message,
        String username,
        String email
) {}
