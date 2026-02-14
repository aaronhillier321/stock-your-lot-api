package com.stockyourlot.controller;

import com.stockyourlot.dto.BillOfSaleExtractionResponse;
import com.stockyourlot.service.BillOfSaleExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bill-of-sale")
public class BillOfSaleExtractionController {

    private final BillOfSaleExtractionService extractionService;

    public BillOfSaleExtractionController(BillOfSaleExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    /**
     * Extract VIN, make, model, trim, color, purchase price, etc. from a bill-of-sale PDF.
     * If uploadToken is provided and OpenAI succeeds, the file is saved to GCS and file_metadata as PENDING.
     */
    @PostMapping("/extract")
    public ResponseEntity<BillOfSaleExtractionResponse> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadToken", required = false) java.util.UUID uploadToken) {
        BillOfSaleExtractionResponse response = extractionService.extractFromPdf(file, uploadToken);
        return ResponseEntity.ok(response);
    }
}
