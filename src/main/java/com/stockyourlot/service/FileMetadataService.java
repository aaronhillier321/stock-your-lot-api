package com.stockyourlot.service;

import com.stockyourlot.dto.FileMetadataResponse;
import com.stockyourlot.entity.Dealership;
import com.stockyourlot.entity.FileMetadata;
import com.stockyourlot.entity.FileStatus;
import com.stockyourlot.entity.FileType;
import com.stockyourlot.entity.Purchase;
import com.stockyourlot.repository.FileMetadataRepository;
import com.stockyourlot.repository.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileMetadataService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB
    private static final Pattern SAFE_PATH_SEGMENT = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final java.util.Set<String> VALID_FILE_TYPES = java.util.Set.of(
            "BILL_OF_SALE", "CONDITION_REPORT", "MISCELLANEOUS");

    private final PurchaseRepository purchaseRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final GcsFileStorageService gcsFileStorageService;

    public FileMetadataService(PurchaseRepository purchaseRepository,
                               FileMetadataRepository fileMetadataRepository,
                               GcsFileStorageService gcsFileStorageService) {
        this.purchaseRepository = purchaseRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.gcsFileStorageService = gcsFileStorageService;
    }

    @Transactional
    public FileMetadataResponse uploadFile(UUID purchaseId, String fileTypeParam, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (fileTypeParam == null || fileTypeParam.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileType is required (BILL_OF_SALE, CONDITION_REPORT, or MISCELLANEOUS)");
        }
        String ft = fileTypeParam.trim().toUpperCase();
        if (!VALID_FILE_TYPES.contains(ft)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileType must be one of: BILL_OF_SALE, CONDITION_REPORT, MISCELLANEOUS");
        }
        FileType fileType = FileType.valueOf(ft);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(PDF_CONTENT_TYPE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed (content-type: application/pdf)");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size must not exceed 20 MB");
        }

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found: " + purchaseId));
        Dealership dealership = purchase.getDealership();
        if (dealership == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase has no dealership; cannot upload file");
        }

        String objectPath = buildObjectPathForPurchase(dealership, purchase.getVin(), fileType);

        byte[] content = file.getBytes();
        gcsFileStorageService.upload(objectPath, content, PDF_CONTENT_TYPE);

        FileMetadata meta = new FileMetadata();
        meta.setPurchase(purchase);
        meta.setDealership(dealership);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = fileType.name() + ".pdf";
        }
        meta.setFileName(originalFilename);
        meta.setFileType(fileType);
        meta.setBucket(gcsFileStorageService.getBucketName());
        meta.setObjectPath(objectPath);
        meta.setContentType(PDF_CONTENT_TYPE);
        meta.setSizeBytes(file.getSize());
        meta = fileMetadataRepository.save(meta);

        return toResponse(meta);
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getByPurchaseId(UUID purchaseId) {
        return fileMetadataRepository.findByPurchase_IdOrderByCreatedAtDesc(purchaseId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponse> getByDealershipId(UUID dealershipId) {
        return fileMetadataRepository.findByDealership_IdOrderByCreatedAtDesc(dealershipId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Claims PENDING file metadata for the given upload token: moves objects from pending path to final path,
     * links them to the purchase and dealership, and sets status to ACTIVE.
     */
    @Transactional
    public void claimPendingFiles(String uploadToken, Purchase purchase) throws IOException {
        if (uploadToken == null || uploadToken.isBlank()) return;
        if (!gcsFileStorageService.isBucketConfigured()) return;

        Dealership dealership = purchase.getDealership();
        if (dealership == null) return;

        List<FileMetadata> pending = fileMetadataRepository.findByUploadTokenAndStatusOrderByCreatedAtAsc(uploadToken, FileStatus.PENDING);
        for (FileMetadata meta : pending) {
            String currentPath = meta.getObjectPath();
            Optional<byte[]> content = gcsFileStorageService.getContent(currentPath);
            if (content.isEmpty()) continue;

            String finalPath = buildObjectPathForPurchase(dealership, purchase.getVin(), meta.getFileType());
            gcsFileStorageService.upload(finalPath, content.get(), meta.getContentType());

            meta.setPurchase(purchase);
            meta.setDealership(dealership);
            meta.setObjectPath(finalPath);
            meta.setStatus(FileStatus.ACTIVE);
            meta.setUploadToken(null);
            fileMetadataRepository.save(meta);

            gcsFileStorageService.delete(currentPath);
        }
    }

    /**
     * Builds object path {safeDealershipName}/{safeVin}/{fileType}.pdf for a purchase.
     */
    private static String buildObjectPathForPurchase(Dealership dealership, String vin, FileType fileType) {
        String dealershipName = dealership.getName();
        if (dealershipName == null || dealershipName.isBlank()) dealershipName = "Unknown";
        String safeDealershipName = SAFE_PATH_SEGMENT.matcher(dealershipName).replaceAll("_").replaceAll("_+", "_");
        if (safeDealershipName.isBlank()) safeDealershipName = "dealership";

        String safeVin = (vin == null || vin.isBlank()) ? "no-vin" : SAFE_PATH_SEGMENT.matcher(vin.trim()).replaceAll("_");
        if (safeVin.isBlank()) safeVin = "no-vin";

        return String.format("%s/%s/%s.pdf", safeDealershipName, safeVin, fileType.name());
    }

    private FileMetadataResponse toResponse(FileMetadata m) {
        return new FileMetadataResponse(
                m.getId(),
                m.getPurchase() != null ? m.getPurchase().getId() : null,
                m.getDealership() != null ? m.getDealership().getId() : null,
                m.getFileName(),
                m.getFileType() != null ? m.getFileType().name() : "MISCELLANEOUS",
                m.getBucket(),
                m.getObjectPath(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getCreatedAt()
        );
    }
}
