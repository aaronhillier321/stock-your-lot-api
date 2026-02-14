package com.stockyourlot.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Matches when GOOGLE_APPLICATION_CREDENTIALS is set and points to an existing file.
 * Used so the GCS Storage bean is only created when credentials are available.
 */
public class GcpCredentialsFilePresentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path == null || path.isBlank()) {
            return false;
        }
        try {
            return Files.isRegularFile(Paths.get(path));
        } catch (Exception e) {
            return false;
        }
    }
}
