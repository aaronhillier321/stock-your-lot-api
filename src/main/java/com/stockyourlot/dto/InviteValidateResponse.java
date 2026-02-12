package com.stockyourlot.dto;

import java.util.UUID;

/**
 * Response when validating an invite token (e.g. for the "set password" page).
 */
public record InviteValidateResponse(
        boolean valid,
        String email,
        UUID dealershipId,
        String dealershipName,
        String message
) {
    public static InviteValidateResponse valid(String email, UUID dealershipId, String dealershipName) {
        return new InviteValidateResponse(true, email, dealershipId, dealershipName, null);
    }

    public static InviteValidateResponse invalid(String message) {
        return new InviteValidateResponse(false, null, null, null, message);
    }
}
