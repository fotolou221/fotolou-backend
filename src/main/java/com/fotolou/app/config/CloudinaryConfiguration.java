package com.fotolou.app.config;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Spring pour l'intégration Cloudinary (stockage et CDN des images).
 */
@Configuration
public class CloudinaryConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CloudinaryConfiguration.class);

    @Bean
    public Cloudinary cloudinary(ApplicationProperties applicationProperties) {
        ApplicationProperties.Storage.CloudinaryProperties props = applicationProperties.getStorage().getCloudinary();

        if (props.getUrl() != null && !props.getUrl().isBlank()) {
            LOG.info("☁️ Cloudinary initialisé via CLOUDINARY_URL");
            return new Cloudinary(props.getUrl());
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", props.getCloudName() != null ? props.getCloudName() : "");
        config.put("api_key", props.getApiKey() != null ? props.getApiKey() : "");
        config.put("api_secret", props.getApiSecret() != null ? props.getApiSecret() : "");
        config.put("secure", true);

        if (props.getCloudName() != null && !props.getCloudName().isBlank()) {
            LOG.info("☁️ Cloudinary configuré avec cloud_name: {}", props.getCloudName());
        } else {
            LOG.info("ℹ️ Cloudinary configuré en attente des clés API (mode fallback local disponible)");
        }

        return new Cloudinary(config);
    }
}
