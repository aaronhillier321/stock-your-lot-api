package com.stockyourlot.service;

/**
 * Sends invite emails. Implement with real SMTP or a provider (SendGrid, SES, etc.).
 */
public interface InviteEmailService {

    /**
     * Send an invite email to the given address with a link to accept the invite.
     *
     * @param toEmail  recipient
     * @param acceptUrl full URL the user should click (e.g. https://app.example.com/accept-invite?token=xxx)
     */
    void sendInviteEmail(String toEmail, String acceptUrl);
}
