package com.fotolou.app.service.custom.queue.impl;

import com.fotolou.app.domain.Relative;
import com.fotolou.app.domain.Salon;
import com.fotolou.app.domain.Ticket;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.SalonStatus;
import com.fotolou.app.domain.enumeration.TicketCategory;
import com.fotolou.app.domain.enumeration.TicketOwnerType;
import com.fotolou.app.domain.enumeration.TicketStatus;
import com.fotolou.app.repository.RelativeRepository;
import com.fotolou.app.repository.SalonRepository;
import com.fotolou.app.repository.TicketRepository;
import com.fotolou.app.service.UserService;
import com.fotolou.app.service.custom.otp.OtpService;
import com.fotolou.app.service.custom.push.BrowserPushService;
import com.fotolou.app.service.custom.queue.QueueEngineService;
import com.fotolou.app.service.custom.sms.SmsService;
import com.fotolou.app.service.dto.TicketDTO;
import com.fotolou.app.service.mapper.TicketMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final List<TicketStatus> ACTIVE_QUEUE_STATUSES = List.of(TicketStatus.YOUR_TURN, TicketStatus.WAITING);

    private record LockedTicket(Ticket ticket, Salon salon) {}

    private record TicketBeneficiary(String name, TicketOwnerType ownerType, Long relativeId, String phone) {}

    private final TicketRepository ticketRepository;
    private final SalonRepository salonRepository;
    private final RelativeRepository relativeRepository;
    private final UserService userService;
    private final TicketMapper ticketMapper;
    private final SmsService smsService;
    private final OtpService otpService;
    private final com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService;
    private final com.fotolou.app.repository.AppNotificationRepository appNotificationRepository;
    private final com.fotolou.app.service.mapper.AppNotificationMapper appNotificationMapper;
    private final com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository;
    private final BrowserPushService browserPushService;

    public QueueEngineServiceImpl(
        TicketRepository ticketRepository,
        SalonRepository salonRepository,
        RelativeRepository relativeRepository,
        UserService userService,
        TicketMapper ticketMapper,
        SmsService smsService,
        OtpService otpService,
        com.fotolou.app.service.custom.realtime.RealtimeEventService realtimeEventService,
        com.fotolou.app.repository.AppNotificationRepository appNotificationRepository,
        com.fotolou.app.service.mapper.AppNotificationMapper appNotificationMapper,
        com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository,
        BrowserPushService browserPushService
    ) {
        this.ticketRepository = ticketRepository;
        this.salonRepository = salonRepository;
        this.relativeRepository = relativeRepository;
        this.userService = userService;
        this.ticketMapper = ticketMapper;
        this.smsService = smsService;
        this.otpService = otpService;
        this.realtimeEventService = realtimeEventService;
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.browserPushService = browserPushService;
    }

    @Override
    public List<TicketDTO> bookTickets(String salonIdOrSlug, String userLogin, List<BeneficiaryItem> beneficiaries) {
        Salon salon = resolveSalon(salonIdOrSlug);
        User currentUser = userLogin != null ? userService.findOneByLogin(userLogin).orElse(null) : null;
        return bookTicketsForSalon(lockSalonForQueueMutation(salon.getId()), currentUser, beneficiaries);
    }

    @Override
    public List<TicketDTO> bookTickets(Long salonId, String userLogin, List<BeneficiaryItem> beneficiaries) {
        User currentUser = userLogin != null ? userService.findOneByLogin(userLogin).orElse(null) : null;
        return bookTickets(salonId, currentUser, beneficiaries);
    }

    @Override
    public List<TicketDTO> bookTickets(Long salonId, User currentUser, List<BeneficiaryItem> beneficiaries) {
        return bookTicketsForSalon(lockSalonForQueueMutation(salonId), currentUser, beneficiaries);
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

    private Salon lockSalonForQueueMutation(Long salonId) {
        if (salonId == null) {
            throw new IllegalArgumentException("Salon requis pour modifier la file d'attente.");
        }

        return salonRepository
            .findByIdForUpdate(salonId)
            .orElseThrow(() -> new IllegalArgumentException("Salon introuvable ID : " + salonId));
    }

    private LockedTicket lockQueueAndLoadTicket(Long ticketId) {
        Long salonId = ticketRepository
            .findSalonIdByTicketId(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));
        Salon salon = lockSalonForQueueMutation(salonId);
        Ticket ticket = ticketRepository
            .findOneWithToOneRelationships(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable ID : " + ticketId));
        return new LockedTicket(ticket, salon);
    }

    private List<TicketDTO> bookTicketsForSalon(Salon salon, User currentUser, List<BeneficiaryItem> beneficiaries) {
        if (salon.getStatus() == SalonStatus.CLOSED) {
            throw new IllegalStateException("Le salon est actuellement fermé aux réservations.");
        }

        List<BeneficiaryItem> requestedBeneficiaries =
            beneficiaries == null || beneficiaries.isEmpty() ? List.of(new BeneficiaryItem("Moi", "SELF", null, null)) : beneficiaries;
        List<TicketBeneficiary> ticketBeneficiaries = normalizeBeneficiaries(requestedBeneficiaries, currentUser);

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInQueueOrderForUpdate(salon.getId(), ACTIVE_QUEUE_STATUSES);

        ZoneId ticketDayZone = ZoneId.systemDefault();
        LocalDate ticketDay = LocalDate.now(ticketDayZone);
        Instant startOfDay = ticketDay.atStartOfDay(ticketDayZone).toInstant();
        Instant startOfNextDay = ticketDay.plusDays(1).atStartOfDay(ticketDayZone).toInstant();
        int nextNumber =
            Optional.ofNullable(ticketRepository.findMaxTicketNumberForSalonAndDay(salon.getId(), startOfDay, startOfNextDay)).orElse(0) +
            1;
        boolean chairOccupied = activeTickets.stream().anyMatch(t -> t.getStatus() == TicketStatus.YOUR_TURN);
        int currentPeopleAhead = activeTickets.size();
        // 1. Empêcher les doublons au sein de la demande elle-même
        Set<String> requestNames = new HashSet<>();
        for (TicketBeneficiary b : ticketBeneficiaries) {
            String bName = b.name();
            if (!requestNames.add(bName.toLowerCase())) {
                throw new IllegalArgumentException(
                    "Impossible de réserver plusieurs tickets pour la même personne ('" + bName + "') dans la même file."
                );
            }
        }

        // 2. Empêcher un utilisateur de prendre deux tickets pour lui-même dans la file de ce salon
        boolean requestingSelf = ticketBeneficiaries.stream().anyMatch(b -> b.ownerType() == TicketOwnerType.SELF);
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
        for (BeneficiaryItem b : requestedBeneficiaries) {
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

        for (TicketBeneficiary b : ticketBeneficiaries) {
            if (b.ownerType() == TicketOwnerType.SELF || b.phone() == null) {
                continue;
            }

            boolean phoneAlreadyActive = activeTickets
                .stream()
                .anyMatch(
                    t -> samePhone(b.phone(), t.getOwnerPhone()) || (t.getUser() != null && samePhone(b.phone(), t.getUser().getLogin()))
                );
            if (phoneAlreadyActive) {
                throw new IllegalStateException("Ce numero a deja un ticket actif dans la file de ce salon.");
            }
        }

        List<Ticket> createdTickets = new ArrayList<>();
        for (TicketBeneficiary b : ticketBeneficiaries) {
            Ticket ticket = new Ticket();
            ticket.setSalon(salon);
            ticket.setUser(currentUser);
            ticket.setTicketNumber(nextNumber++);

            ticket.setOwnerType(b.ownerType());
            ticket.setOwnerName(b.name());
            ticket.setOwnerPhone(b.phone());
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

        recalculateQueueLocked(salon);
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
                sendBeneficiarySmsConfirmation(t, salon, currentUser);
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

        List<TicketDTO> dtos = toDtosWithQueueState(createdTickets);
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
                String normalizedClientPhone = normalizePhoneForCompare(clientPhone);
                String msg = String.format(
                    "Fotolou : Votre ticket #%d chez %s est validé ! %d personne(s) devant vous (~%d min). Suivez votre tour en direct.",
                    created.getTicketNumber(),
                    salonName,
                    created.getPeopleAhead(),
                    created.getEstimatedWaitMinutes()
                );
                smsService.sendSms(normalizedClientPhone != null ? normalizedClientPhone : clientPhone.trim(), msg);
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
        LockedTicket lockedTicket = lockQueueAndLoadTicket(ticketId);
        Ticket ticket = lockedTicket.ticket();

        ticket.setStatus(TicketStatus.YOUR_TURN);
        ticket.setPeopleAhead(0);
        ticket.setEstimatedWaitMinutes(0);
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);

        String salonName = ticket.getSalon() != null ? ticket.getSalon().getName() : "votre salon";
        String smsPhone = ticketSmsPhone(ticket);
        if (smsPhone != null) {
            smsService.sendTicketYourTurnAlert(smsPhone, salonName, ticket.getTicketNumber());
        }

        if (ticket.getUser() != null) {
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

        recalculateQueueLocked(lockedTicket.salon());
        TicketDTO dto = toDtoWithQueueState(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
    public TicketDTO serveTicket(Long ticketId) {
        LockedTicket lockedTicket = lockQueueAndLoadTicket(ticketId);
        Ticket ticket = lockedTicket.ticket();

        ticket.setStatus(TicketStatus.SERVED);
        ticket.setCategory(TicketCategory.HISTORY);
        ticket.setServedAt(Instant.now());
        ticket.setLastModifiedDate(Instant.now());

        Ticket updated = ticketRepository.save(ticket);
        recalculateQueueLocked(lockedTicket.salon());
        TicketDTO dto = toDtoWithQueueState(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
    public TicketDTO cancelTicket(Long ticketId) {
        LockedTicket lockedTicket = lockQueueAndLoadTicket(ticketId);
        Ticket ticket = lockedTicket.ticket();

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

        recalculateQueueLocked(lockedTicket.salon());
        TicketDTO dto = toDtoWithQueueState(updated);
        realtimeEventService.broadcast("TICKET_UPDATED", dto);
        return dto;
    }

    @Override
    public void recalculateQueue(Long salonId) {
        Salon salon = lockSalonForQueueMutation(salonId);
        recalculateQueueLocked(salon);
    }

    private void recalculateQueueLocked(Salon salon) {
        if (salon == null) return;

        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndStatusInQueueOrderForUpdate(salon.getId(), ACTIVE_QUEUE_STATUSES);

        int pos = 0;
        for (Ticket t : activeTickets) {
            int oldAhead = t.getPeopleAhead() != null ? t.getPeopleAhead() : -1;
            TicketStatus oldStatus = t.getStatus();
            if (pos == 0) {
                t.setStatus(TicketStatus.YOUR_TURN);
            } else if (t.getStatus() == TicketStatus.YOUR_TURN) {
                t.setStatus(TicketStatus.WAITING);
            }
            t.setPeopleAhead(pos);
            t.setEstimatedWaitMinutes(pos * DEFAULT_MINUTES_PER_CUT);
            t.setLastModifiedDate(Instant.now());
            ticketRepository.save(t);

            if (pos == 0 && oldStatus != TicketStatus.YOUR_TURN) {
                notifyTicketIsReady(t, salon);
            }

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
        List<TicketDTO> updatedQueueDtos = toDtosWithQueueState(activeTickets);
        realtimeEventService.broadcast("QUEUE_UPDATED", updatedQueueDtos);
    }

    private void notifyTicketIsReady(Ticket ticket, Salon salon) {
        String smsPhone = ticketSmsPhone(ticket);
        if (smsPhone != null) {
            smsService.sendTicketYourTurnAlert(smsPhone, salon.getName(), ticket.getTicketNumber());
        }

        if (ticket.getUser() == null) {
            return;
        }

        sendInAppNotification(
            ticket.getUser(),
            com.fotolou.app.domain.enumeration.RecipientRole.CLIENT,
            com.fotolou.app.domain.enumeration.NotificationType.TICKET,
            "C'est votre tour ! ✂️",
            String.format(
                "Votre coiffeur chez %s vous attend maintenant au fauteuil pour le ticket #%d !",
                salon.getName(),
                ticket.getTicketNumber()
            ),
            "/client/tickets"
        );
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
            browserPushService.sendToUser(user, saved);
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
                return toDtosWithQueueState(salonTickets);
            }
        }

        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
        return toDtosWithQueueState(tickets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDTO> getSalonQueue(Long salonId) {
        List<Ticket> activeTickets = ticketRepository.findBySalonIdAndCategoryOrderByCreatedDateAscIdAsc(salonId, TicketCategory.ACTIVE);
        return toDtosWithQueueState(activeTickets);
    }

    private List<TicketDTO> toDtosWithQueueState(List<Ticket> tickets) {
        Map<Long, Integer> currentNumbersBySalon = new HashMap<>();
        return tickets
            .stream()
            .map(ticket -> {
                TicketDTO dto = ticketMapper.toDto(ticket);
                Long salonId = ticket.getSalon() != null ? ticket.getSalon().getId() : null;
                if (salonId != null) {
                    dto.setCurrentTicketNumber(currentNumbersBySalon.computeIfAbsent(salonId, this::findCurrentTicketNumber));
                }
                applySelfOwnerName(dto, ticket);
                return dto;
            })
            .toList();
    }

    private TicketDTO toDtoWithQueueState(Ticket ticket) {
        TicketDTO dto = ticketMapper.toDto(ticket);
        if (ticket.getSalon() != null && ticket.getSalon().getId() != null) {
            dto.setCurrentTicketNumber(findCurrentTicketNumber(ticket.getSalon().getId()));
        }
        applySelfOwnerName(dto, ticket);
        return dto;
    }

    private String resolveOwnerName(TicketOwnerType ownerType, String requestedName, User currentUser) {
        if (ownerType == TicketOwnerType.SELF && currentUser != null) {
            String userName = userDisplayName(currentUser);
            if (!userName.isBlank()) {
                return userName;
            }
        }

        return requestedName != null && !requestedName.isBlank() ? requestedName.trim() : "Client";
    }

    private List<TicketBeneficiary> normalizeBeneficiaries(List<BeneficiaryItem> items, User currentUser) {
        String currentUserPhone = normalizePhoneForCompare(currentUser != null ? currentUser.getLogin() : null);
        Set<String> requestedPhones = new HashSet<>();
        List<TicketBeneficiary> normalized = new ArrayList<>();

        for (BeneficiaryItem item : items) {
            TicketOwnerType ownerType = resolveOwnerType(item.type());
            String requestedName = item.name();
            String requestedPhone = normalizeOptionalPhone(item.phone());
            Long relativeId = item.relativeId();

            if (ownerType == TicketOwnerType.RELATIVE && currentUser != null && relativeId != null) {
                Relative relative = relativeRepository
                    .findByIdAndUserLogin(relativeId, currentUser.getLogin())
                    .orElseThrow(() -> new IllegalArgumentException("Ce proche est introuvable ou ne vous appartient pas."));
                if (requestedName == null || requestedName.isBlank()) {
                    requestedName = relative.getName();
                }
                if (requestedPhone == null) {
                    requestedPhone = normalizeOptionalPhone(relative.getPhone());
                }
            }

            if (ownerType == TicketOwnerType.SELF) {
                requestedPhone = currentUserPhone;
            } else if (requestedPhone != null) {
                if (currentUserPhone != null && requestedPhone.equals(currentUserPhone)) {
                    throw new IllegalArgumentException("Vous ne pouvez pas utiliser votre propre numero pour une autre personne.");
                }
                if (!requestedPhones.add(requestedPhone)) {
                    throw new IllegalArgumentException(
                        "Le meme numero de telephone ne peut pas etre utilise pour plusieurs beneficiaires."
                    );
                }
            }

            String ownerName = resolveOwnerName(ownerType, requestedName, currentUser);
            normalized.add(new TicketBeneficiary(ownerName, ownerType, relativeId, requestedPhone));
        }

        return normalized;
    }

    private TicketOwnerType resolveOwnerType(String type) {
        if ("RELATIVE".equalsIgnoreCase(type)) {
            return TicketOwnerType.RELATIVE;
        }
        if ("CUSTOM".equalsIgnoreCase(type)) {
            return TicketOwnerType.CUSTOM;
        }
        return TicketOwnerType.SELF;
    }

    private String normalizeOptionalPhone(String rawPhone) {
        String normalized = otpService.normalizePhoneNumber(rawPhone);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        int digitsCount = normalized.replaceAll("[^0-9]", "").length();
        if (digitsCount < 9) {
            throw new IllegalArgumentException("Numero de telephone invalide.");
        }

        return normalized;
    }

    private String normalizePhoneForCompare(String rawPhone) {
        String normalized = otpService.normalizePhoneNumber(rawPhone);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private boolean samePhone(String normalizedPhone, String existingPhone) {
        String normalizedExistingPhone = normalizePhoneForCompare(existingPhone);
        return normalizedPhone != null && normalizedPhone.equals(normalizedExistingPhone);
    }

    private String ticketSmsPhone(Ticket ticket) {
        String ownerPhone = normalizePhoneForCompare(ticket.getOwnerPhone());
        if (ownerPhone != null) {
            return ownerPhone;
        }
        return ticket.getUser() != null ? normalizePhoneForCompare(ticket.getUser().getLogin()) : null;
    }

    private void sendBeneficiarySmsConfirmation(Ticket ticket, Salon salon, User currentUser) {
        String phone = normalizePhoneForCompare(ticket.getOwnerPhone());
        if (phone == null || currentUser == null || samePhone(phone, currentUser.getLogin())) {
            return;
        }

        try {
            String msg = String.format(
                "Fotolou : Votre ticket #%d chez %s est valide pour %s. %d personne(s) devant vous (~%d min).",
                ticket.getTicketNumber(),
                salon.getName(),
                ticket.getOwnerName(),
                ticket.getPeopleAhead(),
                ticket.getEstimatedWaitMinutes()
            );
            smsService.sendSms(phone, msg);
        } catch (Exception e) {
            LOG.warn("Impossible d'envoyer le SMS au beneficiaire {}: {}", phone, e.getMessage());
        }
    }

    private void applySelfOwnerName(TicketDTO dto, Ticket ticket) {
        if (dto == null || ticket == null || ticket.getOwnerType() != TicketOwnerType.SELF || ticket.getUser() == null) {
            return;
        }

        String userName = userDisplayName(ticket.getUser());
        if (!userName.isBlank()) {
            dto.setOwnerName(userName);
        }
    }

    private String userDisplayName(User user) {
        if (user == null) {
            return "";
        }

        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        return (firstName + (lastName.isBlank() ? "" : " " + lastName)).trim();
    }

    private Integer findCurrentTicketNumber(Long salonId) {
        return ticketRepository
            .findBySalonIdAndStatusInOrderByCreatedDateAscIdAsc(salonId, List.of(TicketStatus.YOUR_TURN, TicketStatus.WAITING))
            .stream()
            .findFirst()
            .map(Ticket::getTicketNumber)
            .orElse(null);
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
