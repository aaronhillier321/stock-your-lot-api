package com.stockyourlot.entity;

/**
 * Status of an invite. Stored as VARCHAR in DB (see invites.status).
 */
public enum InviteStatus {
    PENDING,
    ACCEPTED,
    EXPIRED
}
