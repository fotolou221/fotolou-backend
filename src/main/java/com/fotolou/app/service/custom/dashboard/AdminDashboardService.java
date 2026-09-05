package com.fotolou.app.service.custom.dashboard;

import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.dto.SalonDTO;
import java.util.List;

/**
 * Interface de contrat pour le service de métriques et tableau de bord administrateur.
 */
public interface AdminDashboardService {
    record AdminDashboardStatsDTO(
        long totalSalons,
        long waitingTicketsCount,
        long servedTodayCount,
        long totalOrdersCount,
        long totalRevenue,
        long activeClientsCount,
        List<SalonDTO> liveSalons,
        List<BoutiqueOrderDTO> recentOrders
    ) {}

    /**
     * Récupère l'ensemble des statistiques consolidées pour l'administration.
     */
    AdminDashboardStatsDTO getDashboardStats();
}
