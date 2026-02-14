package com.stockyourlot.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class GcsConfig {

    private final ResourceLoader resourceLoader;

    @Value("${app.gcs.credentials-path:}")
    private String credentialsPath;

    public GcsConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Create Storage when credentials are available: from GOOGLE_APPLICATION_CREDENTIALS, or from
     * app.gcs.credentials-path (default classpath:stock-your-lot-f7545b5c7bb7.json), or ADC.
     */
    @Bean
    @Conditional(GcpCredentialsFilePresentCondition.class)
    public Storage storage() throws IOException {
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (envPath != null && !envPath.isBlank() && Files.isRegularFile(Paths.get(envPath))) {
            return StorageOptions.getDefaultInstance().getService();
        }
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try {
                var resource = resourceLoader.getResource(credentialsPath);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                    }
                }
            } catch (IOException e) {
                // Fall through to default instance (e.g. ADC or env var)
            }
        }
        return StorageOptions.getDefaultInstance().getService();
    }
}
