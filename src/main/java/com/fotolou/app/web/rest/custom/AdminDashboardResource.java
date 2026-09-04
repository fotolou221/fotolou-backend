package com.fotolou.app.web.rest.custom;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.*;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.dto.SalonDTO;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
import com.fotolou.app.service.mapper.SalonMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les métriques consolidées du tableau de bord administrateur.
 */
@Tag(name = "8. Administration & Statistiques", description = "KPIs et métriques de la plateforme Fotolou")
@RestController
@RequestMapping("/api/admin")
@Transactional(readOnly = true)
public class AdminDashboardResource {

    private final SalonRepository salonRepository;
    private final TicketRepository ticketRepository;
    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final UserRepository userRepository;
    private final SalonMapper salonMapper;
    private final BoutiqueOrderMapper boutiqueOrderMapper;

    public AdminDashboardResource(
        SalonRepository salonRepository,
        TicketRepository ticketRepository,
        BoutiqueOrderRepository boutiqueOrderRepository,
        UserRepository userRepository,
        SalonMapper salonMapper,
        BoutiqueOrderMapper boutiqueOrderMapper
    ) {
        this.salonRepository = salonRepository;
        this.ticketRepository = ticketRepository;
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.userRepository = userRepository;
        this.salonMapper = salonMapper;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
    }

    public record AdminDashboardStatsDTO(
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
     * GET /api/admin/dashboard-stats : Récupère l'ensemble des statistiques consolidées pour l'administration.
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        long totalSalons = salonRepository.count();

        long waitingTickets = ticketRepository.countBySalonIdAndStatusIn(1L, List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN));

        Instant startOfDay = LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant();
        long totalOrders = boutiqueOrderRepository.count();

        List<BoutiqueOrder> allOrders = boutiqueOrderRepository.findAll();
        long totalRevenue = allOrders.stream().mapToLong(BoutiqueOrder::getTotalPrice).sum();

        long activeClients = userRepository.count();

        List<Salon> salons = salonRepository.findAll();
        List<BoutiqueOrder> recent = boutiqueOrderRepository.findTop10ByOrderByCreatedDateDesc();

        AdminDashboardStatsDTO stats = new AdminDashboardStatsDTO(
            totalSalons,
            waitingTickets,
            0L,
            totalOrders,
            totalRevenue,
            activeClients,
            salons.stream().map(salonMapper::toDto).toList(),
            recent.stream().map(boutiqueOrderMapper::toDto).toList()
        );

        return ResponseEntity.ok(stats);
    }
}
