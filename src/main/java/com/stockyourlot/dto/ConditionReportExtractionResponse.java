package com.stockyourlot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Extracted fields from a condition report PDF (via LLM). Same as bill-of-sale extraction
 * but without purchase price or sale date. Returned to UI for form prefilling.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConditionReportExtractionResponse(
        String vin,
        String make,
        String model,
        String trim,
        String color,
        String auction,
        Integer vehicleYear,
        Integer miles,
        Boolean isValidConditionReport,
        String uploadToken
) {}
