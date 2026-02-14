package com.stockyourlot.repository;

import com.stockyourlot.entity.FileMetadata;
import com.stockyourlot.entity.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByPurchase_IdOrderByCreatedAtDesc(UUID purchaseId);

    List<FileMetadata> findByDealership_IdOrderByCreatedAtDesc(UUID dealershipId);

    List<FileMetadata> findByUploadTokenAndStatusOrderByCreatedAtAsc(String uploadToken, FileStatus status);
}
