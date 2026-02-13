package com.stockyourlot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotNull(message = "dealershipId must not be null")
        UUID dealershipId,
        LocalDate date,
        @Size(max = 100)
        String auctionPlatform,
        @Size(max = 17)
        String vin,
        Integer miles,
        BigDecimal purchasePrice,
        @Size(max = 10)
        String vehicleYear,
        @Size(max = 100)
        String vehicleMake,
        @Size(max = 100)
        String vehicleModel,
        @Size(max = 100)
        String vehicleTrimLevel,
        BigDecimal transportQuote
) {}
