package com.fotolou.app.web.rest.custom;

import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.custom.order.OrderCustomService;
import com.fotolou.app.service.custom.order.OrderCustomService.CheckoutRequest;
import com.fotolou.app.service.custom.order.OrderCustomService.CheckoutResult;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
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
 * Contrôleur REST métier pour les commandes e-commerce de la boutique Fotolou.
 */
@Tag(name = "6. Boutique & Commandes", description = "Gestion du panier et validation des commandes")
@RestController
@RequestMapping("/api")
public class OrderCustomResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCustomResource.class);

    private final OrderCustomService orderCustomService;

    public OrderCustomResource(OrderCustomService orderCustomService) {
        this.orderCustomService = orderCustomService;
    }

    public record UpdateStatusRequest(@NotNull String status) {}

    /**
     * POST /api/orders/checkout : Enregistre une commande et génère le lien de validation WhatsApp.
     */
    @PostMapping("/orders/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {
        try {
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
            CheckoutResult result = orderCustomService.checkout(request, currentLogin);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de la commande", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/orders/my-orders : Récupère l'historique des commandes du client connecté.
     */
    @GetMapping("/orders/my-orders")
    public ResponseEntity<List<BoutiqueOrderDTO>> getMyOrders() {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<BoutiqueOrderDTO> orders = orderCustomService.getMyOrders(currentLogin);
        return ResponseEntity.ok(orders);
    }

    /**
     * PATCH /api/orders/{id}/status : Met à jour le statut d'une commande (admin).
     */
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        try {
            BoutiqueOrderDTO dto = orderCustomService.updateOrderStatus(id, request.status());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
