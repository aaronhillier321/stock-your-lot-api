package com.stockyourlot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDealershipRequest(
        @NotBlank(message = "name must not be blank")
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
        String phone,
        @Valid
        List<DealerPremiumRuleInput> dealerPremiumRules
) {}
