package com.stockyourlot.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for updating a dealership. All fields optional; only provided fields are updated.
 */
public record UpdateDealershipRequest(
        @Size(max = 255)
        String name,
        @Size(max = 255)
        String addressLine1,
        @Size(max = 255)
        String addressLine2,
        @Size(max = 100)
        String city,
        @Size(max = 50)
        String state,
        @Size(max = 20)
        String postalCode,
        @Size(max = 50)
        String phone
) {}
