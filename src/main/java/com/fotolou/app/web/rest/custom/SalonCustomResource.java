package com.fotolou.app.web.rest.custom;

import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.custom.salon.SalonCustomService;
import com.fotolou.app.service.custom.salon.SalonCustomService.ToggleFavoriteResult;
import com.fotolou.app.service.custom.salon.SalonCustomService.ToggleStatusResult;
import com.fotolou.app.service.dto.SalonDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST métier pour les opérations sur les Salons et les Favoris.
 */
@Tag(name = "2. Salons & Barbiers", description = "Recherche de salons et état d'ouverture")
@RestController
@RequestMapping("/api")
public class SalonCustomResource {

    private final SalonCustomService salonCustomService;

    public SalonCustomResource(SalonCustomService salonCustomService) {
        this.salonCustomService = salonCustomService;
    }

    public record ToggleFavoriteRequest(Long salonId) {}

    /**
     * PATCH /api/salons/{id}/toggle-status : Ouvre ou ferme la file du salon (accessible aux coiffeurs & admins).
     */
    @PatchMapping("/salons/{id}/toggle-status")
    public ResponseEntity<?> toggleSalonStatus(@PathVariable Long id) {
        try {
            ToggleStatusResult result = salonCustomService.toggleSalonStatus(id);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/favorites/toggle : Ajoute ou retire un salon des favoris du client connecté.
     */
    @Tag(name = "5. Salons Favoris", description = "Ajout, retrait et consultation des salons favoris")
    @PostMapping("/favorites/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestBody ToggleFavoriteRequest request) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentification requise."));
        }

        try {
            ToggleFavoriteResult result = salonCustomService.toggleFavorite(request.salonId(), currentLogin);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/favorites/my-favorites : Liste complète des salons favoris du client.
     */
    @Tag(name = "5. Salons Favoris", description = "Ajout, retrait et consultation des salons favoris")
    @GetMapping("/favorites/my-favorites")
    public ResponseEntity<List<SalonDTO>> getMyFavorites() {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<SalonDTO> salonList = salonCustomService.getMyFavorites(currentLogin);
        return ResponseEntity.ok(salonList);
    }
}
