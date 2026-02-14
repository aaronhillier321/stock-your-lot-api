package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Extracted fields from a bill-of-sale PDF (via LLM). Returned to UI to prefill the purchase form.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BillOfSaleExtractionResponse(
        String vin,
        String make,
        String model,
        String trim,
        String color,
        BigDecimal purchasePrice,
        String auction,
        Integer vehicleYear,
        Integer miles,
        String saleDate,
        Boolean isValidBillOfSale,
        String uploadToken
) {}
