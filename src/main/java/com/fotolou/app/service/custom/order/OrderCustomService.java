package com.fotolou.app.service.custom.order;

import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Interface de contrat pour les commandes e-commerce de la boutique Fotolou.
 */
public interface OrderCustomService {
    record CartItemRequest(@NotNull Long productId, @NotNull Integer quantity) {}

    record CheckoutRequest(
        @NotEmpty List<CartItemRequest> items,
        String deliveryAddress,
        String deliveryDistrict,
        String orderType, // "WHATSAPP" ou "CALL"
        String customerName,
        String customerPhone,
        String notes
    ) {}

    record AdminCreateOrderRequest(
        @NotEmpty List<CartItemRequest> items,
        String deliveryAddress,
        String deliveryDistrict,
        String orderType,
        @NotNull String customerName,
        @NotNull String customerPhone,
        String notes,
        String status // optionnel : EN_ATTENTE (défaut) ou EN_COURS
    ) {}

    record CheckoutResult(
        Long id,
        String orderNumber,
        Long subtotal,
        Long deliveryFee,
        Long totalPrice,
        String status,
        String orderType,
        String whatsAppUrl,
        BoutiqueOrderDTO order
    ) {}

    /**
     * Enregistre une commande client en statut EN_ATTENTE et génère le lien de confirmation WhatsApp.
     */
    CheckoutResult checkout(CheckoutRequest request, String userLogin);

    /**
     * Création d'une commande par l'admin pour un client qui appelle / passe commande hors application.
     */
    BoutiqueOrderDTO adminCreateOrder(AdminCreateOrderRequest request, String adminLogin);

    /**
     * Confirme une commande EN_ATTENTE (passage en EN_COURS). Idempotent si déjà confirmée.
     */
    BoutiqueOrderDTO confirmOrder(Long orderId, String actorLogin);

    /**
     * Récupère l'historique des commandes d'un client.
     */
    List<BoutiqueOrderDTO> getMyOrders(String userLogin);

    /**
     * Met à jour le statut d'une commande (admin).
     */
    BoutiqueOrderDTO updateOrderStatus(Long orderId, String status);
}
