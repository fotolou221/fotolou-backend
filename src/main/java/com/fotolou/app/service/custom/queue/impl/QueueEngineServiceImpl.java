package com.fotolou.app.service.custom.queue.impl;

import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.custom.queue.QueueEngineService;
import com.fotolou.app.service.custom.sms.SmsService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant le moteur de file d'attente intelligente (Queue Engine).
 */
@Service
@Transactional
public class QueueEngineServiceImpl implements QueueEngineService {

    private static final Logger LOG = LoggerFactory.getLogger(QueueEngineServiceImpl.class);
    private static final int DEFAULT_MINUTES_PER_CUT = 20;

    private final TicketRepository ticketRepository;
    private final SalonRepository salonRepository;
    private final UserService userService;
    private final TicketMapper ticketMapper;
    private final SmsService smsService;
    private final com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService;
    private final com.fotolou.app.repository.AppNotificationRepository appNotificationRepository;
    private final com.fotolou.app.service.mapper.AppNotificationMapper appNotificationMapper;
    private final com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository;

    public QueueEngineServiceImpl(
        TicketRepository ticketRepository,
        SalonRepository salonRepository,
        UserService userService,
        TicketMapper ticketMapper,
        SmsService smsService,
        com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService,
        com.fotolou.app.repository.AppNotificationRepository appNotificationRepository,
        com.fotolou.app.service.mapper.AppNotificationMapper appNotificationMapper,
        com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.salonRepository = salonRepository;
        this.userService = userService;
        this.ticketMapper = ticketMapper;
        this.smsService = smsService;
        this.realtimeEventService = realtimeEventService;
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
        this.coiffeurProfileRepository = coiffeurProfileRepository;
    }

    @Override
    public List<TicketDTO> bookTickets(String salonIdOrSlug, String userLogin, List<BeneficiaryItem> beneficiaries) {
        Salon salon = resolveSalon(salonIdOrSlug);
        User currentUser = userLogin != null ? userService.findOneByLogin(userLogin).orElse(null) : null;
        return bookTicketsForSalon(salon, currentUser, beneficiaries);
    }

    @Override
    public List<TicketDTO> bookTickets(Long salonId, String userLogin, List<BeneficiaryItem> beneficiaries) {
        User currentUser = userLogin != null ? userService.findOneByLogin(userLogin).orElse(null) : null;
        return bookTickets(salonId, currentUser, beneficiaries);
    }

    @Override
    public List<TicketDTO> bookTickets(Long salonId, User currentUser, List<BeneficiaryItem> beneficiaries) {
        Salon salon = salonRepository
            .findById(salonId)
            .orElseThrow(() -> new IllegalArgumentException("Salon introuvable ID : " + salonId));
        return bookTicketsForSalon(salon, currentUser, beneficiaries);
    }

    private Salon resolveSalon(String salonIdOrSlug) {
        if (salonIdOrSlug == null || salonIdOrSlug.isBlank()) {
            throw new IllegalArgumentException("Identifiant de salon manquant.");
        }
        try {
            Long id = Long.parseLong(salonIdOrSlug);
            Optional<Salon> byId = salonRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        } catch (NumberFormatException ignored) {
            // Not a numeric ID, search by slug
        }
        return salonRepository
            .findOneBySlug(salonIdOrSlug)
            .orElseThrow(() -> new IllegalArgumentException("Salon introuvable : " + salonIdOrSlug));
    }

    private List<TicketDTO> bookTicketsForSalon(Salon salon, User currentUser, List<BeneficiaryItem> beneficiaries) {
        if (salon.getStatus() == SalonStatus.CLOSED) {
            throw new IllegalStateException("Le salon est actuellement fermé aux réservations.");
        }

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInOrderByTicketNumberAsc(
            salon.getId(),
            List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN)
        );

        int nextNumber = activeTickets.size() + 1;
        boolean chairOccupied = activeTickets.stream().anyMatch(t -> t.getStatus() == TicketStatus.YOUR_TURN);
        int currentPeopleAhead = activeTickets.size();
        // 1. Empêcher les doublons au sein de la demande elle-même
        Set<String> requestNames = new HashSet<>();
        for (BeneficiaryItem b : beneficiaries) {
            String bName = b.name() != null ? b.name().trim() : "Client";
            if (!requestNames.add(bName.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Impossible de réserver plusieurs tickets pour la même personne ('" + bName + "') dans la même file."
                );
            }
        }

        // 2. Empêcher un utilisateur de prendre deux tickets pour lui-même dans la file de ce salon
        boolean requestingSelf = beneficiaries
            .stream()
            .anyMatch(b -> "SELF".equalsIgnoreCase(b.type()) || (b.name() != null && b.name().toLowerCase().startsWith("moi")));
        if (requestingSelf && currentUser != null) {
            boolean alreadyInQueueSelf = activeTickets
                .stream()
                .anyMatch(
                    t ->
                        (t.getUser() != null &&
                            t.getUser().getId().equals(currentUser.getId()) &&
                            t.getOwnerType() == TicketOwnerType.SELF) ||
                        (t.getOwnerName() != null &&
                            t.getOwnerName().toLowerCase().startsWith("moi") &&
                            t.getUser() != null &&
                            t.getUser().getId().equals(currentUser.getId()))
                );
            if (alreadyInQueueSelf) {
                throw new IllegalStateException("Vous avez déjà un ticket actif dans la file d'attente de ce salon.");
            }
        }

        // 3. Empêcher un doublon de nom avec une personne déjà active dans la file de ce salon (hors compte personnel SELF géré par la règle 2)
        for (BeneficiaryItem b : beneficiaries) {
            String bType = b.type() != null ? b.type().trim().toUpperCase() : "SELF";
            String bName = b.name() != null ? b.name().trim() : "Client";
            boolean isSelf = "SELF".equalsIgnoreCase(bType) || bName.equalsIgnoreCase("moi") || bName.equalsIgnoreCase("moi-même");
            if (!isSelf) {
                boolean nameAlreadyActive = activeTickets
                    .stream()
                    .anyMatch(t -> t.getOwnerName() != null && t.getOwnerName().trim().equalsIgnoreCase(bName));
                if (nameAlreadyActive) {
                    throw new IllegalStateException("Un ticket actif existe déjà pour '" + bName + "' dans la file de ce salon.");
                }
            }
        }

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
            ticket.setCategory(TicketCategory.ACTIVE);
            ticket.setItemCount(1);
            ticket.setCreatedDate(Instant.now());

            if (!chairOccupied && currentPeopleAhead == 0) {
                // Le salon est vide, le 1er client passe directement au fauteuil !
                ticket.setStatus(TicketStatus.YOUR_TURN);
                ticket.setPeopleAhead(0);
                ticket.setEstimatedWaitMinutes(0);
                chairOccupied = true; // Pour que les tickets suivants soient en attente
            } else {
                ticket.setStatus(TicketStatus.WAITING);
                ticket.setPeopleAhead(currentPeopleAhead);
                ticket.setEstimatedWaitMinutes(currentPeopleAhead * DEFAULT_MINUTES_PER_CUT);
            }

            Ticket saved = ticketRepository.save(ticket);
            createdTickets.add(saved);
            currentPeopleAhead++;
        }

        recalculateQueue(salon.getId());
        LOG.info(
            "🎟️ {} ticket(s) réservé(s) pour le salon {} (IDs: {})",
            createdTickets.size(),
            salon.getName(),
            createdTickets.stream().map(Ticket::getId).toList()
        );

        // 1. Notification In-App Client
        for (Ticket t : createdTickets) {
            if (currentUser != null) {
                if (t.getStatus() == TicketStatus.YOUR_TURN) {
                    sendInAppNotification(
                        currentUser,
                        com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                        com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                        "C'est votre tour ! ✂️",
                        String.format("Le fauteuil est libre chez %s pour %s ! Installez-vous.", salon.getName(), t.getOwnerName()),
                        "/client/tickets"
                    );
                } else if (t.getPeopleAhead() != null && t.getPeopleAhead() == 1) {
                    sendInAppNotification(
                        currentUser,
                        com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                        com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                        "Votre tour approche ! ⏳",
                        String.format(
                            "Il n'y a plus qu'une seule personne devant %s chez %s. Veuillez vous rapprocher du salon !",
                            t.getOwnerName(),
                            salon.getName()
                        ),
                        "/client/tickets"
                    );
                } else {
                    sendInAppNotification(
                        currentUser,
                        com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                        com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                        "Ticket #" + t.getTicketNumber() + " confirmé ! 🎟️",
                        String.format(
                            "Votre ticket pour %s (%s) est réservé. Vous avez %d personne(s) devant vous.",
                            salon.getName(),
                            t.getOwnerName(),
                            t.getPeopleAhead()
                        ),
                        "/client/tickets"
                    );
                }
            }
        }

        // 2. Notification In-App Coiffeur Propriétaire
        List<com.fotolou.app.domain.CoiffeurProfile> coiffeurs = coiffeurProfileRepository.findBySalonId(salon.getId());
        for (com.fotolou.app.domain.CoiffeurProfile cp : coiffeurs) {
            if (cp.getUser() != null) {
                sendInAppNotification(
                    cp.getUser(),
                    com.fotolou.app.domain.enumeration.RecipientRole.COIFFEUR,
                    com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                    "Nouveau client en file d'attente 👥",
                    String.format(
                        "%d nouveau(x) ticket(s) pris pour votre salon %s. Total en file : %d personne(s).",
                        createdTickets.size(),
                        salon.getName(),
                        salon.getPeopleWaiting()
                    ),
                    "/coiffeur/tickets"
                );
            }
        }

        List<TicketDTO> dtos = createdTickets.stream().map(ticketMapper::toDto).toList();
        realtimeEventService.broadcast("TICKET_CREATED", dtos);
        return dtos;
    }

    @Override
    public TicketDTO addWalkInClient(Long salonId, String clientName, String clientPhone) {
        String finalName =
            clientName != null && !clientName.isBlank()
                ? clientName.trim()
                : clientPhone != null && !clientPhone.isBlank()
                  ? "Client (" + clientPhone.trim() + ")"
                  : "Client direct";

        BeneficiaryItem walkIn = new BeneficiaryItem(
            finalName,
            "CUSTOM",
            null,
            clientPhone != null && !clientPhone.isBlank() ? clientPhone.trim() : null
        );
        List<TicketDTO> list = bookTickets(salonId, (User) null, List.of(walkIn));
        TicketDTO created = list.get(0);

        if (clientPhone != null && !clientPhone.isBlank()) {
            try {
                Salon salon = salonRepository.findById(salonId).orElse(null);
                String salonName = salon != null ? salon.getName() : "votre salon";
                String msg = String.format(
                    "Fotolou : Votre ticket #%d chez %s est validé ! %d personne(s) devant vous (~%d min). Suivez votre tour en direct.",
                    created.getTicketNumber(),
                    salonName,
                    created.getPeopleAhead(),
                    created.getEstimatedWaitMinutes()
                );
                smsService.sendSms(clientPhone.trim(), msg);
            } catch (Exception e) {
                LOG.warn("Impossible d'envoyer le SMS au client direct {}: {}", clientPhone, e.getMessage());
            }
        }
        return created;
    }

    @Override
    public TicketDTO addWalkInClient(Long salonId, String clientName) {
        return addWalkInClient(salonId, clientName, null);
    }

    @Override
    public TicketDTO callNextTicket(Long ticketId) {
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));

        ticket.setStatus(TicketStatus.YOUR_TURN);
        ticket.setPeopleAhead(0);
        ticket.setEstimatedWaitMinutes(0);
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);

        if (ticket.getUser() != null && ticket.getUser().getLogin() != null) {
            String phone = ticket.getUser().getLogin();
            String salonName = ticket.getSalon() != null ? ticket.getSalon().getName() : "votre salon";
            smsService.sendTicketYourTurnAlert(phone, salonName, ticket.getTicketNumber());

            sendInAppNotification(
                ticket.getUser(),
                com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                "C'est votre tour ! ✂️",
                String.format(
                    "Votre coiffeur chez %s vous attend maintenant au fauteuil pour le ticket #%d !",
                    salonName,
                    ticket.getTicketNumber()
                ),
                "/client/tickets"
            );
        }

        recalculateQueue(ticket.getSalon().getId());
        TicketDTO dto = ticketMapper.toDto(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
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
        TicketDTO dto = ticketMapper.toDto(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCategory(TicketCategory.HISTORY);
        ticket.setCancelledAt(Instant.now());
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);

        if (ticket.getUser() != null) {
            sendInAppNotification(
                ticket.getUser(),
                com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                "Ticket annulé",
                String.format(
                    "Votre ticket #%d chez %s a été annulé avec succès.",
                    ticket.getTicketNumber(),
                    ticket.getSalon() != null ? ticket.getSalon().getName() : "le salon"
                ),
                "/client/tickets"
            );
        }

        if (ticket.getSalon() != null) {
            List<com.fotolou.app.domain.CoiffeurProfile> coiffeurs = coiffeurProfileRepository.findBySalonId(ticket.getSalon().getId());
            for (com.fotolou.app.domain.CoiffeurProfile cp : coiffeurs) {
                if (cp.getUser() != null) {
                    sendInAppNotification(
                        cp.getUser(),
                        com.fotolou.app.domain.enumeration.RecipientRole.COIFFEUR,
                        com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                        "Désistement en file ⚠️",
                        String.format(
                            "Le client du ticket #%d (%s) a quitté la file d'attente.",
                            ticket.getTicketNumber(),
                            ticket.getOwnerName()
                        ),
                        "/coiffeur/tickets"
                    );
                }
            }
        }

        recalculateQueue(ticket.getSalon().getId());
        TicketDTO dto = ticketMapper.toDto(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
    public void recalculateQueue(Long salonId) {
        Salon salon = salonRepository.findById(salonId).orElse(null);
        if (salon == null) return;

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInOrderByTicketNumberAsc(
            salonId,
            List.of(TicketStatus.YOUR_TURN, TicketStatus.WAITING)
        );

        int pos = 0;
        for (Ticket t : activeTickets) {
            int oldAhead = t.getPeopleAhead() != null ? t.getPeopleAhead() : -1;
            t.setPeopleAhead(pos);
            t.setTicketNumber(pos + 1);
            t.setEstimatedWaitMinutes(pos * DEFAULT_MINUTES_PER_CUT);
            ticketRepository.save(t);

            // Alerte in-app proactive : il ne reste plus qu'une seule personne devant le client
            if (pos == 1 && oldAhead > 1 && t.getUser() != null) {
                sendInAppNotification(
                    t.getUser(),
                    com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
                    com.fotolou.app.domain.enumeration.NotificationType.TICKET,
                    "Votre tour approche ! ⏳",
                    String.format(
                        "Il ne reste plus qu'une seule personne devant vous chez %s. Veuillez vous rapprocher du salon !",
                        salon.getName()
                    ),
                    "/client/tickets"
                );
            }
            pos++;
        }

        updateSalonAffluence(salon);
        List<TicketDTO> updatedQueueDtos = activeTickets.stream().map(ticketMapper::toDto).toList();
        realtimeEventService.broadcast("QUEUE_UPDATED", updatedQueueDtos);
    }

    private void sendInAppNotification(
        User user,
        com.fotolou.app.domain.enumeration.RecipientRole role,
        com.fotolou.app.domain.enumeration.NotificationType type,
        String title,
        String message,
        String targetRoute
    ) {
        try {
            com.fotolou.app.domain.AppNotification notif = new com.fotolou.app.domain.AppNotification();
            notif.setUser(user);
            notif.setRecipientRole(role);
            notif.setType(type);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setIsRead(false);
            notif.setTargetRoute(targetRoute);
            notif.setCreatedDate(Instant.now());
            com.fotolou.app.domain.AppNotification saved = appNotificationRepository.save(notif);
            realtimeEventService.broadcast("NOTIFICATION_CREATED", appNotificationMapper.toDto(saved));
            LOG.info("🔔 Notification In-App créée [{}] : {}", role, title);
        } catch (Exception e) {
            LOG.warn("⚠️ Erreur création notification in-app : {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getMyTickets(String userLogin) {
        if (userLogin == null) {
            return Collections.emptyList();
        }
        Optional<User> optUser = userService.findOneByLogin(userLogin);
        if (optUser.isEmpty()) {
            return Collections.emptyList();
        }

        User user = optUser.get();
        boolean isCoiffeur = user
            .getAuthorities()
            .stream()
            .anyMatch(a -> com.fotolou.app.security.AuthoritiesConstants.COIFFEUR.equals(a.getName()));

        if (isCoiffeur) {
            Optional<com.fotolou.app.domain.CoiffeurProfile> cpOpt = coiffeurProfileRepository.findOneWithSalonByUserLogin(userLogin);
            if (cpOpt.isPresent() && cpOpt.get().getSalon() != null) {
                Long salonId = cpOpt.get().getSalon().getId();
                List<Ticket> salonTickets = ticketRepository.findBySalonIdOrderByCreatedDateDesc(salonId);
                return salonTickets.stream().map(ticketMapper::toDto).toList();
            }
        }

        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
        return tickets.stream().map(ticketMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getSalonQueue(Long salonId) {
        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndCategoryOrderByTicketNumberAsc(salonId, TicketCategory.ACTIVE);
        return activeTickets.stream().map(ticketMapper::toDto).toList();
    }

    private void updateSalonAffluence(Salon salon) {
        long countWaiting = ticketRepository.countBySalonIdAndStatusIn(
            salon.getId(),
            List.of(TicketStatus.WAITING, TicketStatus.YOUR_TURN)
        );
        salon.setPeopleWaiting((int) countWaiting);
        salon.setEstimatedWaitMinutes((int) countWaiting * DEFAULT_MINUTES_PER_CUT);
        Salon saved = salonRepository.save(salon);

        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("id", saved.getId());
        payload.put("slug", saved.getSlug() != null ? saved.getSlug() : "");
        payload.put("peopleWaiting", saved.getPeopleWaiting());
        payload.put("estimatedWaitMinutes", saved.getEstimatedWaitMinutes());
        payload.put("status", saved.getStatus().name());

        realtimeEventService.broadcast("SALON_UPDATED", payload);
    }
}
