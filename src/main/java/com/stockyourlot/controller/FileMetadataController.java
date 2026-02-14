package com.stockyourlot.controller;

import com.stockyourlot.dto.FileMetadataResponse;
import com.stockyourlot.service.FileMetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileMetadataController {

    private final FileMetadataService fileMetadataService;

    public FileMetadataController(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    /**
     * Upload a PDF for a specific purchase. File is stored in GCS under dealership/purchase path.
     * fileType must be one of: BILL_OF_SALE, CONDITION_REPORT, MISCELLANEOUS.
     * Requires authentication.
     */
    @PostMapping("/purchases/{purchaseId}/files")
    public ResponseEntity<FileMetadataResponse> uploadFile(
            @PathVariable UUID purchaseId,
            @RequestParam("fileType") String fileType,
            @RequestParam("file") MultipartFile file) throws IOException {
        FileMetadataResponse response = fileMetadataService.uploadFile(purchaseId, fileType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List file metadata for a purchase.
     */
    @GetMapping("/purchases/{purchaseId}/files")
    public ResponseEntity<List<FileMetadataResponse>> getFilesByPurchase(@PathVariable UUID purchaseId) {
        return ResponseEntity.ok(fileMetadataService.getByPurchaseId(purchaseId));
    }

    /**
     * List file metadata for a dealership.
     */
    @GetMapping("/dealerships/{dealershipId}/files")
    public ResponseEntity<List<FileMetadataResponse>> getFilesByDealership(@PathVariable UUID dealershipId) {
        return ResponseEntity.ok(fileMetadataService.getByDealershipId(dealershipId));
    }
}
