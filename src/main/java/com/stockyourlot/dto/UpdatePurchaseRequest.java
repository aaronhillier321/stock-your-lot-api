package com.stockyourlot.dto;

import com.stockyourlot.entity.PurchaseStatus;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for updating a purchase. All fields optional; only provided fields are updated.
 */
public record UpdatePurchaseRequest(
        PurchaseStatus status,
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
