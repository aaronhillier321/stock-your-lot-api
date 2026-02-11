package com.stockyourlot.dto;

/**
 * Result of a registration attempt: either success with the response, or conflict with an error message.
 */
public record RegisterResult(
        boolean success,
        RegisterResponse response,
        String errorMessage
) {
    public static RegisterResult success(RegisterResponse response) {
        return new RegisterResult(true, response, null);
    }

    public static RegisterResult conflict(String errorMessage) {
        return new RegisterResult(false, null, errorMessage);
    }
}
