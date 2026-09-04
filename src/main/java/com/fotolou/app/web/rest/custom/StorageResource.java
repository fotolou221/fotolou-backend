package com.fotolou.app.web.rest.custom;

import com.fotolou.app.service.custom.storage.StorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrôleur REST pour le téléversement et la distribution des fichiers et images.
 */
@Tag(name = "9. Stockage & Fichiers", description = "Téléversement et distribution de fichiers")
@RestController
@RequestMapping("/api")
public class StorageResource {

    private static final Logger LOG = LoggerFactory.getLogger(StorageResource.class);

    private final StorageService storageService;

    public StorageResource(StorageService storageService) {
        this.storageService = storageService;
    }

    public record UploadResponse(String url, String message) {}

    /**
     * POST /api/storage/upload : Téléverse une image (avatar, salon, produit).
     */
    @PostMapping(value = { "/storage/upload", "/files/upload" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "folder", required = false, defaultValue = "media") String folder
    ) {
        try {
            String fileUrl = storageService.store(file, folder);
            return ResponseEntity.ok(new UploadResponse(fileUrl, "Fichier téléversé avec succès."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Erreur téléversement fichier", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Échec du téléversement du fichier."));
        }
    }

    /**
     * GET /api/files/** : Distribue le fichier demandé en streaming HTTP.
     */
    @GetMapping("/files/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getRequestURI().indexOf("/api/files/") + 11);
        try {
            Resource file = storageService.loadAsResource(path);
            String contentType = Files.probeContentType(file.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
