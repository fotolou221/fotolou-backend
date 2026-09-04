package com.fotolou.app.service.custom.queue;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.custom.sms.SmsService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moteur de gestion temps réel de la file d'attente virtuelle Fotolou.
 */
@Service
@Transactional
public class QueueEngineService {

    private static final Logger LOG = LoggerFactory.getLogger(QueueEngineService.class);
    private static final int DEFAULT_MINUTES_PER_CUT = 20;

    private final TicketRepository ticketRepository;
    private final SalonRepository salonRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final SmsService smsService;

    public QueueEngineService(
        TicketRepository ticketRepository,
        SalonRepository salonRepository,
        UserRepository userRepository,
        TicketMapper ticketMapper,
        SmsService smsService
    ) {
        this.ticketRepository = ticketRepository;
        this.salonRepository = salonRepository;
        this.userRepository = userRepository;
        this.ticketMapper = ticketMapper;
        this.smsService = smsService;
    }

    public record BeneficiaryItem(
        String name,
        String type, // "SELF", "RELATIVE", "CUSTOM"
        Long relativeId,
        String phone
    ) {}

    /**
     * Réservation de tickets (individuelle ou groupée pour proches).
     */
    public List<TicketDTO> bookTickets(Long salonId, User currentUser, List<BeneficiaryItem> beneficiaries) {
        Salon salon = salonRepository
            .findById(salonId)
            .orElseThrow(() -> new IllegalArgumentException("Salon introuvable ID : " + salonId));

        if (salon.getStatus() == SalonStatus.CLOSED) {
            throw new IllegalStateException("Le salon est actuellement fermé aux réservations.");
        }

        Instant startOfDay = LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant();
        Integer currentMaxNumber = ticketRepository.findMaxTicketNumberForSalonAndDay(salonId, startOfDay);
        int nextNumber = currentMaxNumber != null ? currentMaxNumber + 1 : 1;

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInOrderByTicketNumberAsc(
            salonId,
            List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN)
        );

        int currentPeopleAhead = activeTickets.size();
        List<Ticket> createdTickets = new ArrayList<>();

        for (BeneficiaryItem b : beneficiaries) {
            Ticket ticket = new Ticket();
            ticket.setSalon(salon);
            ticket.setUser(currentUser);
            ticket.setTicketNumber(nextNumber++);
            ticket.setOwnerName(b.name() != null && !b.name().isBlank() ? b.name() : "Client");

            TicketOwnerType ownerType = TicketOwnerType.SELF;
            if ("RELATIVE".equalsIgnoreCase(b.type())) {
                ownerType = TicketOwnerType.RELATIVE;
            } else if ("CUSTOM".equalsIgnoreCase(b.type())) {
                ownerType = TicketOwnerType.CUSTOM;
            }
            ticket.setOwnerType(ownerType);

            ticket.setStatus(TicketStatus.WAITING);
            ticket.setCategory(TicketCategory.ACTIVE);
            ticket.setPeopleAhead(currentPeopleAhead);
            ticket.setEstimatedWaitMinutes(currentPeopleAhead * DEFAULT_MINUTES_PER_CUT);
            ticket.setItemCount(1);
            ticket.setCreatedDate(Instant.now());

            Ticket saved = ticketRepository.save(ticket);
            createdTickets.add(saved);
            currentPeopleAhead++;
        }

        updateSalonAffluence(salon);
        LOG.info(
            "🎟️ {} ticket(s) réservé(s) pour le salon {} (IDs: {})",
            createdTickets.size(),
            salon.getName(),
            createdTickets.stream().map(Ticket::getId).toList()
        );

        return createdTickets.stream().map(ticketMapper::toDto).toList();
    }

    /**
     * Ajout d'un client venu sur place (Walk-in) par le coiffeur.
     */
    public TicketDTO addWalkInClient(Long salonId, String clientName) {
        BeneficiaryItem walkIn = new BeneficiaryItem(
            clientName != null && !clientName.isBlank() ? clientName : "Client direct",
            "CUSTOM",
            null,
            null
        );
        List<TicketDTO> list = bookTickets(salonId, null, List.of(walkIn));
        return list.get(0);
    }

    /**
     * Appel du client (C'est votre tour).
     */
    public TicketDTO callNextTicket(Long ticketId) {
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));

        ticket.setStatus(TicketStatus.YOUR_TURN);
        ticket.setPeopleAhead(0);
        ticket.setEstimatedWaitMinutes(0);
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);

        // Notification SMS si le numéro de téléphone est disponible
        if (ticket.getUser() != null && ticket.getUser().getLogin() != null) {
            String phone = ticket.getUser().getLogin();
            String salonName = ticket.getSalon() != null ? ticket.getSalon().getName() : "votre salon";
            smsService.sendTicketYourTurnAlert(phone, salonName, ticket.getTicketNumber());
        }

        recalculateQueue(ticket.getSalon().getId());
        return ticketMapper.toDto(updated);
    }

    /**
     * Marquer un ticket comme servi avec succès.
     */
    public TicketDTO serveTicket(Long ticketId) {
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));

        ticket.setStatus(TicketStatus.SERVED);
        ticket.setCategory(TicketCategory.HISTORY);
        ticket.setServedAt(Instant.now());
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);
        recalculateQueue(ticket.getSalon().getId());
        return ticketMapper.toDto(updated);
    }

    /**
     * Annulation d'un ticket (par le client ou le coiffeur).
     */
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCategory(TicketCategory.HISTORY);
        ticket.setCancelledAt(Instant.now());
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);
        recalculateQueue(ticket.getSalon().getId());
        return ticketMapper.toDto(updated);
    }

    /**
     * Recalcul automatique et atomique de toutes les positions dans la file du salon.
     */
    public void recalculateQueue(Long salonId) {
        Salon salon = salonRepository.findById(salonId).orElse(null);
        if (salon == null) return;

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInOrderByTicketNumberAsc(
            salonId,
            List.of(TicketStatus.YOUR_TURN, TicketStatus.WAITING)
        );

        int pos = 0;
        for (Ticket t : activeTickets) {
            t.setPeopleAhead(pos);
            t.setEstimatedWaitMinutes(pos * DEFAULT_MINUTES_PER_CUT);
            ticketRepository.save(t);
            pos++;
        }

        updateSalonAffluence(salon);
    }

    private void updateSalonAffluence(Salon salon) {
        long countWaiting = ticketRepository.countBySalonIdAndStatusIn(
            salon.getId(),
            List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN)
        );
        salon.setPeopleWaiting((int) countWaiting);
        salon.setEstimatedWaitMinutes((int) countWaiting * DEFAULT_MINUTES_PER_CUT);
        salonRepository.save(salon);
    }
}
