package com.stockyourlot.config;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates secure invite tokens and hashes for storage.
 * Raw token is sent in the email link; only the hash is stored in the DB.
 */
@Component
public class InviteTokenUtil {

    private static final int TOKEN_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new random token (base64url, safe for URLs).
     * Caller stores hashToken(token) in DB and sends this value in the email link.
     */
    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash a token for storage. Use this when persisting an invite.
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " not available", e);
        }
    }

    /**
     * Verify that the raw token matches the stored hash.
     */
    public boolean verifyToken(String rawToken, String storedHash) {
        return storedHash != null && storedHash.equals(hashToken(rawToken));
    }
}
