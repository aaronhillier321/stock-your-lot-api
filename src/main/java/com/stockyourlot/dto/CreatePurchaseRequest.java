package com.stockyourlot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotNull(message = "dealershipId must not be null")
        UUID dealershipId,
        @NotNull(message = "date must not be null")
        LocalDate date,
        @NotNull(message = "auctionPlatform must not be null")
        @Size(max = 100)
        String auctionPlatform,
        @NotNull(message = "vin must not be null")
        @Size(max = 17)
        String vin,
        Integer miles,
        @NotNull(message = "purchasePrice must not be null")
        BigDecimal purchasePrice,
        @Size(max = 10)
        String vehicleYear,
        @Size(max = 100)
        String vehicleMake,
        @Size(max = 100)
        String vehicleModel,
        @Size(max = 100)
        String vehicleTrimLevel,
        BigDecimal transportQuote,
        /** Optional. When present, pending files uploaded with this token are claimed and linked to the new purchase. */
        UUID uploadToken
) {}
