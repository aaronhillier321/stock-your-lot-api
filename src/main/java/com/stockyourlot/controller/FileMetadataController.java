package com.stockyourlot.controller;

import com.stockyourlot.dto.FileMetadataResponse;
import com.stockyourlot.service.FileMetadataService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    /**
     * Get full file content by file metadata ID. Uses metadata to resolve the object in GCS and returns the file
     * with appropriate Content-Type and Content-Disposition. Requires authentication.
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable UUID fileId) throws UnsupportedEncodingException {
        FileMetadataService.FileDownload download = fileMetadataService.getFileContent(fileId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(download.contentType()));
        String encodedFileName = URLEncoder.encode(download.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + download.fileName() + "\"; filename*=UTF-8''" + encodedFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(download.content());
    }
}
