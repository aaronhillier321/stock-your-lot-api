package com.stockyourlot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Logs the invite link instead of sending email. Use when no SMTP is configured.
 * Disable by setting app.invite.email.stub=false and providing a real implementation.
 */
@Service
@ConditionalOnProperty(name = "app.invite.email.stub", havingValue = "true", matchIfMissing = true)
public class StubInviteEmailService implements InviteEmailService {

    private static final Logger log = LoggerFactory.getLogger(StubInviteEmailService.class);

    @Override
    public void sendInviteEmail(String toEmail, String acceptUrl) {
        log.info("=== INVITE EMAIL (stub) === To: {} | Accept link: {}", toEmail, acceptUrl);
    }
}
