package com.fotolou.app.web.rest.custom;

import com.fotolou.app.service.custom.publicstats.PublicStatsService;
import com.fotolou.app.service.custom.publicstats.PublicStatsService.PublicStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST public pour les statistiques réelles de la plateforme Fotolou (site vitrine).
 */
@Tag(name = "11. Statistiques Publiques & Vitrine", description = "Endpoints publics exposant les métriques réelles pour le site vitrine")
@RestController
@RequestMapping("/api")
public class PublicStatsResource {

    private final PublicStatsService publicStatsService;

    public PublicStatsResource(PublicStatsService publicStatsService) {
        this.publicStatsService = publicStatsService;
    }

    /**
     * GET /api/public/stats ou /api/vitrine/stats : Récupère les statistiques consolidées publiques en temps réel.
     */
    @Operation(summary = "Statistiques publiques réelles de la plateforme (pour la vitrine)")
    @GetMapping(value = { "/public/stats", "/vitrine/stats" })
    public ResponseEntity<PublicStatsDTO> getPublicStats() {
        PublicStatsDTO stats = publicStatsService.getPublicStats();
        return ResponseEntity.ok(stats);
    }
}
