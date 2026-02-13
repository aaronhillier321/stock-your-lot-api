package com.stockyourlot.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseResponse(
        UUID id,
        UUID buyerId,
        UUID dealershipId,
        String dealershipName,
        LocalDate date,
        String auctionPlatform,
        String vin,
        Integer miles,
        BigDecimal purchasePrice,
        String vehicleYear,
        String vehicleMake,
        String vehicleModel,
        String vehicleTrimLevel,
        BigDecimal transportQuote,
        Instant createdAt
) {}
