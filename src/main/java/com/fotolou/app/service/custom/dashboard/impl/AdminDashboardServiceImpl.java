package com.fotolou.app.service.custom.dashboard.impl;

import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.custom.dashboard.AdminDashboardService;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
import com.fotolou.app.service.mapper.SalonMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service de tableau de bord administrateur.
 */
@Service
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final SalonRepository salonRepository;
    private final TicketRepository ticketRepository;
    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final UserRepository userRepository;
    private final SalonMapper salonMapper;
    private final BoutiqueOrderMapper boutiqueOrderMapper;

    public AdminDashboardServiceImpl(
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

    @Override
    public AdminDashboardStatsDTO getDashboardStats() {
        long totalSalons = salonRepository.count();

        long waitingTickets = ticketRepository.countBySalonIdAndStatusIn(1L, List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN));

        long totalOrders = boutiqueOrderRepository.count();

        List<BoutiqueOrder> allOrders = boutiqueOrderRepository.findAll();
        long totalRevenue = allOrders.stream().mapToLong(BoutiqueOrder::getTotalPrice).sum();

        long activeClients = userRepository.count();

        List<Salon> salons = salonRepository.findAll();
        List<BoutiqueOrder> recent = boutiqueOrderRepository.findTop10ByOrderByCreatedDateDesc();

        return new AdminDashboardStatsDTO(
            totalSalons,
            waitingTickets,
            0L,
            totalOrders,
            totalRevenue,
            activeClients,
            salons.stream().map(salonMapper::toDto).toList(),
            recent.stream().map(boutiqueOrderMapper::toDto).toList()
        );
    }
}
