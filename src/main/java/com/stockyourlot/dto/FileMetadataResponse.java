package com.stockyourlot.dto;

import java.time.Instant;
import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        UUID purchaseId,
        UUID dealershipId,
        String fileName,
        String fileType,
        String bucket,
        String objectPath,
        String contentType,
        Long sizeBytes,
        Instant createdAt
) {}
