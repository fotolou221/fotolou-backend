package com.fotolou.app.service.custom.publicstats;

/**
 * Interface pour les statistiques publiques de la plateforme Fotolou (site vitrine).
 */
public interface PublicStatsService {
    record PublicStatsDTO(
        long activeUsers,
        String activeUsersFormatted,
        long totalSalons,
        String totalSalonsFormatted,
        double satisfactionRate,
        String satisfactionRateFormatted,
        String serviceAvailability,
        long totalTicketsServed,
        long totalOrders
    ) {}

    /**
     * Récupère les métriques publiques pour la vitrine Fotolou.
     */
    PublicStatsDTO getPublicStats();
}
