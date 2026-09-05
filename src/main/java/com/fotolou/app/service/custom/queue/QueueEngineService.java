package com.fotolou.app.service.custom.queue;

import com.fotolou.app.domain.User;
import com.fotolou.app.service.dto.TicketDTO;
import java.util.List;

/**
 * Interface du moteur de gestion temps réel de la file d'attente virtuelle Fotolou.
 */
public interface QueueEngineService {
    record BeneficiaryItem(
        String name,
        String type, // "SELF", "RELATIVE", "CUSTOM"
        Long relativeId,
        String phone
    ) {}

    /**
     * Réservation de tickets avec objet User.
     */
    List<TicketDTO> bookTickets(Long salonId, User currentUser, List<BeneficiaryItem> beneficiaries);

    /**
     * Réservation de tickets avec login utilisateur.
     */
    List<TicketDTO> bookTickets(Long salonId, String userLogin, List<BeneficiaryItem> beneficiaries);

    /**
     * Réservation de tickets avec ID numérique ou slug de salon.
     */
    List<TicketDTO> bookTickets(String salonIdOrSlug, String userLogin, List<BeneficiaryItem> beneficiaries);

    /**
     * Ajout d'un client venu sur place (Walk-in) par le coiffeur.
     */
    TicketDTO addWalkInClient(Long salonId, String clientName);

    /**
     * Appel du client (C'est votre tour).
     */
    TicketDTO callNextTicket(Long ticketId);

    /**
     * Marquer un ticket comme servi avec succès.
     */
    TicketDTO serveTicket(Long ticketId);

    /**
     * Annulation d'un ticket (par le client ou le coiffeur).
     */
    TicketDTO cancelTicket(Long ticketId);

    /**
     * Recalcul automatique et atomique de toutes les positions dans la file du salon.
     */
    void recalculateQueue(Long salonId);

    /**
     * Récupère la liste des tickets d'un utilisateur par son identifiant de connexion.
     */
    List<TicketDTO> getMyTickets(String userLogin);

    /**
     * Récupère la liste des tickets actifs dans la file d'un salon.
     */
    List<TicketDTO> getSalonQueue(Long salonId);
}
