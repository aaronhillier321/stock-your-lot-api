package com.stockyourlot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Sends invite emails via SMTP. Active when app.invite.email.stub=false.
 * Configure spring.mail.* and app.invite.email.from in application properties.
 */
@Service
@ConditionalOnProperty(name = "app.invite.email.stub", havingValue = "false")
public class SmtpInviteEmailService implements InviteEmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpInviteEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.invite.email.from:}")
    private String fromEmail;

    @Value("${app.invite.email.subject:You're invited to Stock Your Lot}")
    private String subject;

    public SmtpInviteEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInviteEmail(String toEmail, String acceptUrl) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("app.invite.email.from must be set when using SMTP invite emails");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainBody(acceptUrl), htmlBody(acceptUrl));
            mailSender.send(message);
            log.info("Invite email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send invite email to {}", toEmail, e);
            throw new RuntimeException("Failed to send invite email", e);
        }
    }

    private static String plainBody(String acceptUrl) {
        return "You've been invited to join Stock Your Lot.\n\n"
                + "Click the link below to set your password and activate your account:\n\n"
                + acceptUrl + "\n\n"
                + "This link expires in 7 days. If you didn't expect this email, you can ignore it.";
    }

    private static String htmlBody(String acceptUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: sans-serif; line-height: 1.5; color: #333;">
            <p>You've been invited to join <strong>Stock Your Lot</strong>.</p>
            <p>Click the link below to set your password and activate your account:</p>
            <p><a href="%s" style="color: #0066cc;">Accept invite and set password</a></p>
            <p style="font-size: 0.9em; color: #666;">This link expires in 7 days. If you didn't expect this email, you can ignore it.</p>
            </body>
            </html>
            """.formatted(acceptUrl);
    }
}
