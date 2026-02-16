package com.stockyourlot.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DealershipResponse(
        UUID id,
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String phone,
        Instant createdAt,
        long purchaseCount,
        List<DealerPremiumAssignmentDto> dealerPremiumRules
) {}
