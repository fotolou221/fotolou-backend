package com.fotolou.app.web.rest.custom;

import com.fotolou.app.service.custom.dashboard.AdminDashboardService;
import com.fotolou.app.service.custom.dashboard.AdminDashboardService.AdminDashboardStatsDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les métriques consolidées du tableau de bord administrateur.
 */
@Tag(name = "8. Administration & Statistiques", description = "KPIs et métriques de la plateforme Fotolou")
@RestController
@RequestMapping("/api/admin")
public class AdminDashboardResource {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardResource(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    /**
     * GET /api/admin/dashboard-stats : Récupère l'ensemble des statistiques consolidées pour l'administration.
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        AdminDashboardStatsDTO stats = adminDashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}
