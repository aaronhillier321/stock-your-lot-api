package com.stockyourlot.dto;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseRequest(
        @Size(max = 255)
        String dealership,
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
