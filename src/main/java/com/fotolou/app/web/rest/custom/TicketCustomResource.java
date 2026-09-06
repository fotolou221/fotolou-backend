package com.fotolou.app.web.rest.custom;

import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.custom.queue.QueueEngineService;
import com.fotolou.app.service.custom.queue.QueueEngineService.BeneficiaryItem;
import com.fotolou.app.service.dto.TicketDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints REST métier pour la file d'attente et la gestion des tickets en direct.
 */
@Tag(name = "3. File d'Attente & Tickets", description = "Prise de ticket et gestion de la file")
@RestController
@RequestMapping("/api")
public class TicketCustomResource {

    private static final Logger LOG = LoggerFactory.getLogger(TicketCustomResource.class);

    private final QueueEngineService queueEngineService;

    public TicketCustomResource(QueueEngineService queueEngineService) {
        this.queueEngineService = queueEngineService;
    }

    public record BookMultipleTicketsRequestVM(Object salonId, String salonSlug, List<BeneficiaryItem> beneficiaries) {}

    public record WalkInRequestVM(@NotNull Long salonId, String clientName, String clientPhone) {}

    /**
     * POST /api/tickets/book-multiple : Prise groupée de tickets pour soi et/ou ses proches.
     */
    @PostMapping("/tickets/book-multiple")
    public ResponseEntity<?> bookMultipleTickets(@Valid @RequestBody BookMultipleTicketsRequestVM request) {
        try {
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);

            List<BeneficiaryItem> items = request.beneficiaries();
            if (items == null || items.isEmpty()) {
                items = List.of(new BeneficiaryItem("Moi", "SELF", null, null));
            }

            String identifier;
            if (request.salonSlug() != null && !request.salonSlug().isBlank()) {
                identifier = request.salonSlug();
            } else if (request.salonId() != null && !request.salonId().toString().isBlank()) {
                identifier = request.salonId().toString();
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "salonId ou salonSlug requis"));
            }

            List<TicketDTO> tickets = queueEngineService.bookTickets(identifier, currentLogin, items);
            return ResponseEntity.status(HttpStatus.CREATED).body(tickets);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Erreur réservation ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Une erreur est survenue lors de la réservation du ticket.")
            );
        }
    }

    /**
     * POST /api/tickets/walk-in : Ajout d'un client venu directement sur place par le barbier.
     */
    @PostMapping("/tickets/walk-in")
    public ResponseEntity<?> addWalkInClient(@Valid @RequestBody WalkInRequestVM request) {
        try {
            TicketDTO ticket = queueEngineService.addWalkInClient(request.salonId(), request.clientName(), request.clientPhone());
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (Exception e) {
            LOG.error("Erreur ajout walk-in", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tickets/{id}/call-next : Appel du client par le coiffeur ou admin.
     */
    @PostMapping("/tickets/{id}/call-next")
    public ResponseEntity<?> callNextTicket(@PathVariable Long id) {
        try {
            TicketDTO ticket = queueEngineService.callNextTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            LOG.error("Erreur appel ticket", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tickets/{id}/serve : Marquer le ticket comme servi avec succès.
     */
    @PostMapping("/tickets/{id}/serve")
    public ResponseEntity<?> serveTicket(@PathVariable Long id) {
        try {
            TicketDTO ticket = queueEngineService.serveTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            LOG.error("Erreur service ticket", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/tickets/{id}/cancel : Annuler un ticket et libérer la place dans la file.
     */
    @PostMapping("/tickets/{id}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id) {
        try {
            TicketDTO ticket = queueEngineService.cancelTicket(id);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            LOG.error("Erreur annulation ticket", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/tickets/my-tickets : Récupérer tous les tickets de l'utilisateur connecté.
     */
    @GetMapping("/tickets/my-tickets")
    public ResponseEntity<List<TicketDTO>> getMyTickets() {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<TicketDTO> dtos = queueEngineService.getMyTickets(currentLogin);
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /api/salons/{salonId}/queue : Récupérer la file d'attente active d'un salon.
     */
    @GetMapping("/salons/{salonId}/queue")
    public ResponseEntity<List<TicketDTO>> getSalonQueue(@PathVariable Long salonId) {
        List<TicketDTO> dtos = queueEngineService.getSalonQueue(salonId);
        return ResponseEntity.ok(dtos);
    }
}
