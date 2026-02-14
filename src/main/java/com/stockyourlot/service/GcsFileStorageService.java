package com.stockyourlot.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Uploads file content to Google Cloud Storage. Supports pending paths for extract flow.
 * Storage is optional (null when credentials file is not available); GCS operations are no-ops then.
 */
@Service
public class GcsFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(GcsFileStorageService.class);

    public static final String PENDING_PREFIX = "pending/";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final Storage storage;

    @Value("${app.gcs.bucket:}")
    private String bucketName;

    public GcsFileStorageService(@Autowired(required = false) Storage storage) {
        this.storage = storage;
    }

    public String getBucketName() {
        return bucketName;
    }

    public boolean isBucketConfigured() {
        return storage != null && bucketName != null && !bucketName.isBlank();
    }

    /**
     * Upload bytes to GCS at the given path within the configured bucket.
     */
    public void upload(String objectPath, byte[] content, String contentType) throws IOException {
        if (!isBucketConfigured()) {
            throw new IllegalStateException("GCS is not configured (app.gcs.bucket and credentials required)");
        }
        BlobId blobId = BlobId.of(bucketName, objectPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, content);
    }

    /**
     * Upload a PDF to pending path: pending/{uploadToken}/{fileName}.
     */
    public void uploadPending(String uploadToken, String fileName, byte[] content) throws IOException {
        if (!isBucketConfigured()) return;
        String path = pendingPath(uploadToken, fileName);
        upload(path, content, PDF_CONTENT_TYPE);
    }

    public static String pendingPath(String uploadToken, String fileName) {
        return PENDING_PREFIX + uploadToken + "/" + fileName;
    }

    /**
     * Get content of an object, or empty if it does not exist.
     */
    public Optional<byte[]> getContent(String objectPath) {
        log.debug("getContent: bucket={}, objectPath={}, isBucketConfigured={}, storageNull={}",
                bucketName, objectPath, isBucketConfigured(), storage == null);
        if (!isBucketConfigured()) {
            log.warn("getContent: skipped - GCS not configured (bucket={})", bucketName);
            return Optional.empty();
        }
        if (storage == null) {
            log.warn("getContent: skipped - Storage bean is null");
            return Optional.empty();
        }
        BlobId blobId = BlobId.of(bucketName, objectPath);
        var blob = storage.get(blobId);
        if (blob == null) {
            log.warn("getContent: blob not found in GCS for bucket={}, objectPath={}", bucketName, objectPath);
            return Optional.empty();
        }
        log.debug("getContent: found blob in GCS, size={} bytes", blob.getSize());
        return Optional.of(blob.getContent());
    }

    /**
     * Delete an object if it exists.
     */
    public void delete(String objectPath) {
        if (!isBucketConfigured() || storage == null) return;
        storage.delete(BlobId.of(bucketName, objectPath));
    }
}
