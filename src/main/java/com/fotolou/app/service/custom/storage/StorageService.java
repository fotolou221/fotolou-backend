package com.fotolou.app.service.custom.storage;

import com.fotolou.app.config.ApplicationProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service de stockage et téléversement de fichiers (avatars, bannières, produits).
 */
@Service
public class StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/svg+xml");

    private final Path rootLocation;
    private final ApplicationProperties applicationProperties;

    public StorageService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        String uploadDir = applicationProperties.getStorage().getUploadDir();
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            LOG.error("Impossible d'initialiser le dossier de stockage", e);
        }
    }

    /**
     * Enregistre un fichier téléversé et retourne son URL d'accès public.
     */
    public String store(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Format de fichier non supporté. Formats acceptés : JPEG, PNG, WebP, SVG.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        } else {
            extension = ".webp";
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path targetDir = folder != null && !folder.isBlank() ? this.rootLocation.resolve(folder) : this.rootLocation;
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(uniqueFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = folder != null && !folder.isBlank() ? folder + "/" + uniqueFilename : uniqueFilename;
        String publicUrl = applicationProperties.getStorage().getCdnUrl() + relativePath;

        LOG.info("📁 Fichier enregistré : {} -> {}", targetPath, publicUrl);
        return publicUrl;
    }

    /**
     * Charge une ressource de fichier pour la distribution HTTP.
     */
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Fichier introuvable : " + filename);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible de lire le fichier : " + filename, e);
        }
    }
}
