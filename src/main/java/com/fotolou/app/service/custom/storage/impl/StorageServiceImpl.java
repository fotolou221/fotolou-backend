package com.fotolou.app.service.custom.storage.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.service.custom.storage.StorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implémentation du service de stockage et téléversement de fichiers.
 * Gère le stockage cloud Cloudinary et le stockage local sur disque avec bascule automatique.
 */
@Service
public class StorageServiceImpl implements StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(StorageServiceImpl.class);
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/svg+xml");

    private final Path rootLocation;
    private final ApplicationProperties applicationProperties;
    private final Cloudinary cloudinary;

    public StorageServiceImpl(ApplicationProperties applicationProperties, Cloudinary cloudinary) {
        this.applicationProperties = applicationProperties;
        this.cloudinary = cloudinary;
        String uploadDir = applicationProperties.getStorage().getUploadDir();
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            LOG.error("Impossible d'initialiser le dossier de stockage local", e);
        }
    }

    @Override
    public String store(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Format de fichier non supporté. Formats acceptés : JPEG, PNG, WebP, SVG.");
        }

        boolean isCloudinary = "cloudinary".equalsIgnoreCase(applicationProperties.getStorage().getProvider());
        String cloudName = applicationProperties.getStorage().getCloudinary().getCloudName();
        String cloudinaryUrl = applicationProperties.getStorage().getCloudinary().getUrl();
        boolean hasCloudinaryCreds = (cloudName != null && !cloudName.isBlank()) || (cloudinaryUrl != null && !cloudinaryUrl.isBlank());

        // 1. Téléversement Cloudinary si actif et configuré
        if (isCloudinary && hasCloudinaryCreds) {
            try {
                String targetFolder = folder != null && !folder.isBlank() ? "fotolou/" + folder : "fotolou";
                Map<?, ?> uploadParams = ObjectUtils.asMap("folder", targetFolder, "resource_type", "auto");
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
                String secureUrl = (String) uploadResult.get("secure_url");
                LOG.info("☁️ [CLOUDINARY] Image téléversée avec succès sur le CDN : {}", secureUrl);
                return secureUrl;
            } catch (Exception e) {
                LOG.error("❌ Échec du téléversement sur Cloudinary, bascule automatique sur le stockage local : {}", e.getMessage());
            }
        }

        // 2. Stockage local (par défaut si provider 'local' ou si Cloudinary indisponible)
        return storeLocally(file, folder);
    }

    private String storeLocally(MultipartFile file, String folder) throws IOException {
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

        LOG.info("📁 [LOCAL] Fichier enregistré localement : {} -> {}", targetPath, publicUrl);
        return publicUrl;
    }

    @Override
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

    @Override
    public String getActiveProvider() {
        return applicationProperties.getStorage().getProvider();
    }
}
