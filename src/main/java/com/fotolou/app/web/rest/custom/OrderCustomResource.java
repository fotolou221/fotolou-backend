package com.fotolou.app.web.rest.custom;

import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.OrderItem;
import com.fotolou.app.domain.Product;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.OrderStatus;
import com.fotolou.app.domain.enumeration.OrderType;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.repository.OrderItemRepository;
import com.fotolou.app.repository.ProductRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.SecurityUtils;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST métier pour les commandes e-commerce de la boutique Fotolou.
 */
@Tag(name = "6. Boutique & Commandes", description = "Gestion du panier et validation des commandes")
@RestController
@RequestMapping("/api")
@Transactional
public class OrderCustomResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCustomResource.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BoutiqueOrderMapper boutiqueOrderMapper;
    private final ApplicationProperties applicationProperties;

    public OrderCustomResource(
        BoutiqueOrderRepository boutiqueOrderRepository,
        OrderItemRepository orderItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        BoutiqueOrderMapper boutiqueOrderMapper,
        ApplicationProperties applicationProperties
    ) {
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
        this.applicationProperties = applicationProperties;
    }

    public record CartItemRequest(@NotNull Long productId, @NotNull Integer quantity) {}

    public record CheckoutRequestVM(
        @NotEmpty List<CartItemRequest> items,
        String deliveryAddress,
        String deliveryDistrict,
        String orderType, // "WHATSAPP" ou "CALL"
        String customerName,
        String customerPhone,
        String notes
    ) {}

    public record CheckoutResponseVM(
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

    public record UpdateStatusRequest(@NotNull String status) {}

    /**
     * POST /api/orders/checkout : Enregistre une commande et génère le lien de validation WhatsApp.
     */
    @PostMapping("/orders/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequestVM request) {
        try {
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
            User currentUser = currentLogin != null ? userRepository.findOneByLogin(currentLogin).orElse(null) : null;

            long subtotal = 0L;
            List<OrderItem> orderItems = new ArrayList<>();
            StringBuilder orderSummaryText = new StringBuilder();

            for (CartItemRequest itemReq : request.items()) {
                Product product = productRepository
                    .findById(itemReq.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Produit introuvable ID : " + itemReq.productId()));

                int qty = Math.max(1, itemReq.quantity());
                long itemTotal = product.getPrice() * qty;
                subtotal += itemTotal;

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(qty);
                orderItem.setUnitPrice(product.getPrice());
                orderItem.setProductTitle(product.getTitle());
                orderItems.add(orderItem);

                orderSummaryText.append(String.format("- %s (x%d) : %,d FCFA\n", product.getTitle(), qty, itemTotal));
            }

            long deliveryFee = applicationProperties.getBusiness().getDeliveryFee();
            long totalPrice = subtotal + deliveryFee;

            String orderNumber = "CMD-2026-" + (1000 + RANDOM.nextInt(9000));

            BoutiqueOrder order = new BoutiqueOrder();
            order.setOrderNumber(orderNumber);
            order.setSubtotal(subtotal);
            order.setDeliveryFee(deliveryFee);
            order.setTotalPrice(totalPrice);
            order.setStatus(OrderStatus.EN_COURS);

            OrderType type = "CALL".equalsIgnoreCase(request.orderType()) ? OrderType.CALL : OrderType.WHATSAPP;
            order.setOrderType(type);

            order.setDeliveryAddress(request.deliveryAddress());
            order.setDeliveryDistrict(request.deliveryDistrict());
            order.setCustomerName(
                request.customerName() != null ? request.customerName() : currentUser != null ? currentUser.getFirstName() : "Client"
            );
            order.setCustomerPhone(
                request.customerPhone() != null ? request.customerPhone() : currentUser != null ? currentUser.getLogin() : ""
            );
            order.setNotes(request.notes());
            order.setUser(currentUser);
            order.setCreatedDate(Instant.now());

            BoutiqueOrder savedOrder = boutiqueOrderRepository.save(order);

            for (OrderItem oi : orderItems) {
                oi.setOrder(savedOrder);
                orderItemRepository.save(oi);
            }

            // Construction du lien WhatsApp
            String whatsappPhone = applicationProperties.getBusiness().getWhatsappNumber();
            String messageTemplate = String.format(
                "👋 Bonjour Fotolou ! Je souhaite confirmer ma commande *#%s* :\n\n" +
                    "📦 *Articles :*\n%s\n" +
                    "💰 *Sous-total :* %,d FCFA\n" +
                    "🛵 *Livraison :* %,d FCFA\n" +
                    "💵 *TOTAL :* %,d FCFA\n\n" +
                    "📍 *Adresse de livraison :* %s (%s)\n" +
                    "👤 *Nom :* %s\n" +
                    "📞 *Téléphone :* %s",
                orderNumber,
                orderSummaryText,
                subtotal,
                deliveryFee,
                totalPrice,
                order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "À préciser",
                order.getDeliveryDistrict() != null ? order.getDeliveryDistrict() : "Dakar",
                order.getCustomerName(),
                order.getCustomerPhone()
            );

            String encodedMsg = URLEncoder.encode(messageTemplate, StandardCharsets.UTF_8);
            String whatsAppUrl = "https://wa.me/" + whatsappPhone + "?text=" + encodedMsg;

            BoutiqueOrderDTO orderDTO = boutiqueOrderMapper.toDto(savedOrder);

            LOG.info("🛍️ Commande #{} enregistrée avec succès (Total: {} FCFA)", orderNumber, totalPrice);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new CheckoutResponseVM(
                    savedOrder.getId(),
                    savedOrder.getOrderNumber(),
                    savedOrder.getSubtotal(),
                    savedOrder.getDeliveryFee(),
                    savedOrder.getTotalPrice(),
                    savedOrder.getStatus().name(),
                    savedOrder.getOrderType().name(),
                    whatsAppUrl,
                    orderDTO
                )
            );
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

        User user = userRepository.findOneByLogin(currentLogin).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<BoutiqueOrder> orders = boutiqueOrderRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
        return ResponseEntity.ok(orders.stream().map(boutiqueOrderMapper::toDto).toList());
    }

    /**
     * PATCH /api/orders/{id}/status : Met à jour le statut d'une commande (admin).
     */
    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Optional<BoutiqueOrder> optOrder = boutiqueOrderRepository.findById(id);
        if (optOrder.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoutiqueOrder order = optOrder.get();
        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.status().toUpperCase());
            order.setStatus(newStatus);
            order.setLastModifiedDate(Instant.now());
            boutiqueOrderRepository.save(order);

            LOG.info("📦 Statut de la commande #{} changé en {}", order.getOrderNumber(), newStatus);
            return ResponseEntity.ok(boutiqueOrderMapper.toDto(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Statut invalide : " + request.status()));
        }
    }
}
