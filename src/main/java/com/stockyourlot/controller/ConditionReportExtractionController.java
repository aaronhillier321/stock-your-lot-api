package com.stockyourlot.controller;

import com.stockyourlot.dto.ConditionReportExtractionResponse;
import com.stockyourlot.service.ConditionReportExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/condition-report")
public class ConditionReportExtractionController {

    private final ConditionReportExtractionService extractionService;

    public ConditionReportExtractionController(ConditionReportExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    /**
     * Extract VIN, make, model, trim, color, auction, vehicle year, miles from a condition report PDF.
     * If uploadToken is provided and OpenAI succeeds, the file is saved to GCS and file_metadata as PENDING.
     */
    @PostMapping("/extract")
    public ResponseEntity<ConditionReportExtractionResponse> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadToken", required = false) java.util.UUID uploadToken) {
        ConditionReportExtractionResponse response = extractionService.extractFromPdf(file, uploadToken);
        return ResponseEntity.ok(response);
    }
}
