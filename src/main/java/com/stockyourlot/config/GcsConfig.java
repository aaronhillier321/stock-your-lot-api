package com.stockyourlot.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class GcsConfig {

    /**
     * Create Storage only when GOOGLE_APPLICATION_CREDENTIALS points to an existing file.
     * Otherwise no bean is created so the app can start (e.g. when the optional GCP secret is not mounted).
     */
    @Bean
    @Conditional(GcpCredentialsFilePresentCondition.class)
    public Storage storage() throws IOException {
        return StorageOptions.getDefaultInstance().getService();
    }
}
