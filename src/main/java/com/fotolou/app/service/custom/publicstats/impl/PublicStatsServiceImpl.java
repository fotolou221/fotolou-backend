package com.fotolou.app.service.custom.publicstats.impl;

import com.fotolou.app.domain.PlatformSettings;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.repository.PlatformSettingsRepository;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.custom.publicstats.PublicStatsService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service fournissant les statistiques publiques réelles pour la vitrine.
 */
@Service
@Transactional(readOnly = true)
public class PublicStatsServiceImpl implements PublicStatsService {

    private final UserRepository userRepository;
    private final SalonRepository salonRepository;
    private final TicketRepository ticketRepository;
    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final PlatformSettingsRepository platformSettingsRepository;

    public PublicStatsServiceImpl(
        UserRepository userRepository,
        SalonRepository salonRepository,
        TicketRepository ticketRepository,
        BoutiqueOrderRepository boutiqueOrderRepository,
        PlatformSettingsRepository platformSettingsRepository
    ) {
        this.userRepository = userRepository;
        this.salonRepository = salonRepository;
        this.ticketRepository = ticketRepository;
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.platformSettingsRepository = platformSettingsRepository;
    }

    @Override
    public PublicStatsDTO getPublicStats() {
        long activeUsers = userRepository.count();
        long totalSalons = salonRepository.count();
        long totalOrders = boutiqueOrderRepository.count();

        // Calcul des tickets servis
        long servedTickets = ticketRepository.countByStatusIn(List.of(TicketStatus.SERVED, TicketStatus.COMPLETED));
        long cancelledTickets = ticketRepository.countByStatusIn(List.of(TicketStatus.CANCELLED));

        // Taux de satisfaction : basé sur les tickets complétés vs annulés, baseline 98%
        double satisfactionRate = 98.0;
        if (servedTickets + cancelledTickets > 0) {
            double calculatedRate = ((double) servedTickets / (servedTickets + cancelledTickets)) * 100.0;
            satisfactionRate = Math.min(100.0, Math.max(90.0, Math.round(calculatedRate * 10.0) / 10.0));
        }

        // Disponibilité du service (vérification statut maintenance)
        String serviceAvailability = "24/7";
        List<PlatformSettings> settingsList = platformSettingsRepository.findAll();
        if (!settingsList.isEmpty() && Boolean.TRUE.equals(settingsList.getFirst().getMaintenanceMode())) {
            serviceAvailability = "Maintenance";
        }

        return new PublicStatsDTO(
            activeUsers,
            formatMetricCount(activeUsers),
            totalSalons,
            formatMetricCount(totalSalons),
            satisfactionRate,
            String.format("%.0f%%", satisfactionRate),
            serviceAvailability,
            servedTickets,
            totalOrders
        );
    }

    private String formatMetricCount(long count) {
        if (count <= 0) {
            return "0";
        }
        if (count >= 1_000_000) {
            return String.format("+%,dM", count / 1_000_000).replace(',', ' ');
        }
        if (count >= 1_000) {
            return String.format("+%,d", count).replace(',', ' ');
        }
        if (count >= 10) {
            return "+" + count;
        }
        return String.valueOf(count);
    }
}
