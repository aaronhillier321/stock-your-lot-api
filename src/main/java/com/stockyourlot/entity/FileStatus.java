package com.stockyourlot.entity;

/**
 * Lifecycle status of a file record. PENDING = from extract flow, not yet linked to a purchase; ACTIVE = linked to a purchase.
 */
public enum FileStatus {
    PENDING,
    ACTIVE
}
