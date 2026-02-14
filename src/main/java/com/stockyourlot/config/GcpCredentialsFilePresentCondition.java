package com.stockyourlot.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Matches when GCS credentials are available: either GOOGLE_APPLICATION_CREDENTIALS is unset (use
 * Application Default Credentials, e.g. gcloud auth application-default login) or it points to an
 * existing file. When the env var is set but the file does not exist (e.g. K8s optional secret not
 * mounted), we do not create the bean so the app can still start.
 */
public class GcpCredentialsFilePresentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path == null || path.isBlank()) {
            return true;
        }
        try {
            return Files.isRegularFile(Paths.get(path));
        } catch (Exception e) {
            return false;
        }
    }
}
