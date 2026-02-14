package com.stockyourlot.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Uploads file content to Google Cloud Storage. Supports pending paths for extract flow.
 */
@Service
public class GcsFileStorageService {

    public static final String PENDING_PREFIX = "pending/";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final Storage storage;

    @Value("${app.gcs.bucket:}")
    private String bucketName;

    public GcsFileStorageService(Storage storage) {
        this.storage = storage;
    }

    public String getBucketName() {
        return bucketName;
    }

    public boolean isBucketConfigured() {
        return bucketName != null && !bucketName.isBlank();
    }

    /**
     * Upload bytes to GCS at the given path within the configured bucket.
     */
    public void upload(String objectPath, byte[] content, String contentType) throws IOException {
        if (!isBucketConfigured()) {
            throw new IllegalStateException("app.gcs.bucket must be set to upload files");
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
        if (!isBucketConfigured()) return Optional.empty();
        var blob = storage.get(BlobId.of(bucketName, objectPath));
        if (blob == null) return Optional.empty();
        return Optional.of(blob.getContent());
    }

    /**
     * Delete an object if it exists.
     */
    public void delete(String objectPath) {
        if (!isBucketConfigured()) return;
        storage.delete(BlobId.of(bucketName, objectPath));
    }
}
