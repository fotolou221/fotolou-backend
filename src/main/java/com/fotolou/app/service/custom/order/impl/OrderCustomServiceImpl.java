package com.fotolou.app.service.custom.order.impl;

import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.domain.BoutiqueOrder;
import com.fotolou.app.domain.OrderItem;
import com.fotolou.app.domain.Product;
import com.fotolou.app.domain.User;
import com.fotolou.app.domain.enumeration.NotificationType;
import com.fotolou.app.domain.enumeration.OrderStatus;
import com.fotolou.app.domain.enumeration.OrderType;
import com.fotolou.app.domain.enumeration.RecipientRole;
import com.fotolou.app.repository.AppNotificationRepository;
import com.fotolou.app.repository.BoutiqueOrderRepository;
import com.fotolou.app.repository.OrderItemRepository;
import com.fotolou.app.repository.ProductRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.service.custom.order.OrderCustomService;
import com.fotolou.app.service.custom.push.BrowserPushService;
import com.fotolou.app.service.custom.realtime.RealtimeEventService;
import com.fotolou.app.service.dto.BoutiqueOrderDTO;
import com.fotolou.app.service.mapper.AppNotificationMapper;
import com.fotolou.app.service.mapper.BoutiqueOrderMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service métier pour les commandes e-commerce de la boutique Fotolou.
 *
 * Cycle de vie d'une commande :
 *  - Client : checkout -> EN_ATTENTE (en attente de confirmation WhatsApp / appel)
 *  - Confirmation (client via WhatsApp, ou admin) -> EN_COURS
 *  - Admin : EN_COURS -> LIVRE / ANNULE
 *  - Admin peut créer directement une commande (client au téléphone) en EN_ATTENTE ou EN_COURS.
 * Chaque transition est diffusée en temps réel (ORDER_CREATED / ORDER_UPDATED).
 */
@Service
@Transactional
public class OrderCustomServiceImpl implements OrderCustomService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCustomServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BoutiqueOrderMapper boutiqueOrderMapper;
    private final ApplicationProperties applicationProperties;
    private final AppNotificationRepository appNotificationRepository;
    private final AppNotificationMapper appNotificationMapper;
    private final RealtimeEventService realtimeEventService;
    private final BrowserPushService browserPushService;

    public OrderCustomServiceImpl(
        BoutiqueOrderRepository boutiqueOrderRepository,
        OrderItemRepository orderItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        BoutiqueOrderMapper boutiqueOrderMapper,
        ApplicationProperties applicationProperties,
        AppNotificationRepository appNotificationRepository,
        AppNotificationMapper appNotificationMapper,
        RealtimeEventService realtimeEventService,
        BrowserPushService browserPushService
    ) {
        this.boutiqueOrderRepository = boutiqueOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.boutiqueOrderMapper = boutiqueOrderMapper;
        this.applicationProperties = applicationProperties;
        this.appNotificationRepository = appNotificationRepository;
        this.appNotificationMapper = appNotificationMapper;
        this.realtimeEventService = realtimeEventService;
        this.browserPushService = browserPushService;
    }

    // ──────────────────────────────────────────────────────────────
    // Checkout client
    // ──────────────────────────────────────────────────────────────

    @Override
    public CheckoutResult checkout(CheckoutRequest request, String userLogin) {
        User currentUser = userLogin != null ? userRepository.findOneByLogin(userLogin).orElse(null) : null;

        OrderType type = "CALL".equalsIgnoreCase(request.orderType()) ? OrderType.CALL : OrderType.WHATSAPP;

        BuiltOrder built = buildOrder(
            request.items(),
            request.deliveryAddress(),
            request.deliveryDistrict(),
            type,
            request.customerName() != null ? request.customerName() : currentUser != null ? currentUser.getFirstName() : "Client",
            request.customerPhone() != null ? request.customerPhone() : currentUser != null ? currentUser.getLogin() : "",
            request.notes(),
            currentUser,
            OrderStatus.EN_ATTENTE
        );

        BoutiqueOrder savedOrder = built.order();
        String whatsAppUrl = buildWhatsAppUrl(savedOrder, built.summaryText());
        BoutiqueOrderDTO orderDTO = toDto(savedOrder);
        orderDTO.setWhatsAppUrl(whatsAppUrl);

        if (currentUser != null) {
            sendInAppNotification(
                currentUser,
                RecipientRole.CLIENT,
                NotificationType.ORDER,
                "Commande reçue 🛍️",
                String.format(
                    "Votre commande #%s (%,d FCFA) est en attente de confirmation. Confirmez-la sur WhatsApp pour lancer la préparation.",
                    savedOrder.getOrderNumber(),
                    savedOrder.getTotalPrice()
                ),
                "/client/boutique/commandes"
            );
        }

        realtimeEventService.broadcast("ORDER_CREATED", orderDTO);
        LOG.info("🛍️ Commande #{} créée en attente (Total: {} FCFA)", savedOrder.getOrderNumber(), savedOrder.getTotalPrice());

        return new CheckoutResult(
            savedOrder.getId(),
            savedOrder.getOrderNumber(),
            savedOrder.getSubtotal(),
            savedOrder.getDeliveryFee(),
            savedOrder.getTotalPrice(),
            savedOrder.getStatus().name(),
            savedOrder.getOrderType().name(),
            whatsAppUrl,
            orderDTO
        );
    }

    // ──────────────────────────────────────────────────────────────
    // Création admin (client au téléphone)
    // ──────────────────────────────────────────────────────────────

    @Override
    public BoutiqueOrderDTO adminCreateOrder(AdminCreateOrderRequest request, String adminLogin) {
        OrderType type = "WHATSAPP".equalsIgnoreCase(request.orderType()) ? OrderType.WHATSAPP : OrderType.CALL;
        OrderStatus status = parseStatus(request.status(), OrderStatus.EN_COURS);
        if (status != OrderStatus.EN_ATTENTE && status != OrderStatus.EN_COURS) {
            status = OrderStatus.EN_COURS;
        }

        // rattache la commande à un compte client existant si le numéro correspond
        User linkedUser =
            request.customerPhone() != null ? userRepository.findOneByLogin(request.customerPhone().trim()).orElse(null) : null;

        BuiltOrder built = buildOrder(
            request.items(),
            request.deliveryAddress(),
            request.deliveryDistrict(),
            type,
            request.customerName(),
            request.customerPhone(),
            request.notes(),
            linkedUser,
            status
        );

        BoutiqueOrder savedOrder = built.order();
        BoutiqueOrderDTO orderDTO = toDto(savedOrder);

        if (linkedUser != null) {
            sendInAppNotification(
                linkedUser,
                RecipientRole.CLIENT,
                NotificationType.ORDER,
                "Commande enregistrée 🛍️",
                String.format(
                    "Une commande #%s (%,d FCFA) a été enregistrée pour vous par l'équipe Fotolou.",
                    savedOrder.getOrderNumber(),
                    savedOrder.getTotalPrice()
                ),
                "/client/boutique/commandes"
            );
        }

        realtimeEventService.broadcast("ORDER_CREATED", orderDTO);
        LOG.info(
            "🛍️ Commande admin #{} créée pour {} ({}) - statut {}",
            savedOrder.getOrderNumber(),
            savedOrder.getCustomerName(),
            savedOrder.getCustomerPhone(),
            status
        );
        return orderDTO;
    }

    // ──────────────────────────────────────────────────────────────
    // Confirmation / changement de statut
    // ──────────────────────────────────────────────────────────────

    @Override
    public BoutiqueOrderDTO confirmOrder(Long orderId, String actorLogin) {
        BoutiqueOrder order = boutiqueOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Commande introuvable ID : " + orderId));

        if (order.getStatus() == OrderStatus.EN_ATTENTE) {
            order.setStatus(OrderStatus.EN_COURS);
            order.setLastModifiedDate(Instant.now());
            order = boutiqueOrderRepository.save(order);

            notifyStatusChange(order);
            BoutiqueOrderDTO dto = toDto(order);
            realtimeEventService.broadcast("ORDER_UPDATED", dto);
            LOG.info("📦 Commande #{} confirmée (EN_COURS)", order.getOrderNumber());
            return dto;
        }
        return toDto(order);
    }

    @Override
    public BoutiqueOrderDTO updateOrderStatus(Long orderId, String status) {
        BoutiqueOrder order = boutiqueOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Commande introuvable ID : " + orderId));

        OrderStatus newStatus = parseStatus(status, null);
        if (newStatus == null) {
            throw new IllegalArgumentException("Statut de commande invalide : " + status);
        }

        order.setStatus(newStatus);
        order.setLastModifiedDate(Instant.now());
        BoutiqueOrder saved = boutiqueOrderRepository.save(order);

        notifyStatusChange(saved);
        BoutiqueOrderDTO dto = toDto(saved);
        realtimeEventService.broadcast("ORDER_UPDATED", dto);
        LOG.info("📦 Statut de la commande #{} changé en {}", saved.getOrderNumber(), newStatus);
        return dto;
    }

    // ──────────────────────────────────────────────────────────────
    // Lecture
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BoutiqueOrderDTO> getMyOrders(String userLogin) {
        if (userLogin == null) {
            return Collections.emptyList();
        }
        User user = userRepository.findOneByLogin(userLogin).orElse(null);
        if (user == null) {
            return Collections.emptyList();
        }
        return boutiqueOrderRepository.findByUserIdOrderByCreatedDateDesc(user.getId()).stream().map(this::toDto).toList();
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────

    private record BuiltOrder(BoutiqueOrder order, String summaryText) {}

    private BuiltOrder buildOrder(
        List<CartItemRequest> itemRequests,
        String deliveryAddress,
        String deliveryDistrict,
        OrderType type,
        String customerName,
        String customerPhone,
        String notes,
        User user,
        OrderStatus status
    ) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide.");
        }

        long subtotal = 0L;
        List<OrderItem> orderItems = new ArrayList<>();
        StringBuilder summary = new StringBuilder();

        for (CartItemRequest itemReq : itemRequests) {
            Product product = productRepository
                .findById(itemReq.productId())
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable ID : " + itemReq.productId()));

            if (Boolean.FALSE.equals(product.getInStock())) {
                throw new IllegalStateException("Le produit \"" + product.getTitle() + "\" est en rupture de stock.");
            }

            int qty = Math.max(1, itemReq.quantity());
            long itemTotal = product.getPrice() * qty;
            subtotal += itemTotal;

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(qty);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setProductTitle(product.getTitle());
            orderItems.add(orderItem);

            summary.append(String.format("- %s (x%d) : %,d FCFA%n", product.getTitle(), qty, itemTotal));
        }

        long deliveryFee = applicationProperties.getBusiness().getDeliveryFee();
        long totalPrice = subtotal + deliveryFee;
        String orderNumber = generateOrderNumber();

        BoutiqueOrder order = new BoutiqueOrder();
        order.setOrderNumber(orderNumber);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        order.setOrderType(type);
        order.setDeliveryAddress(deliveryAddress);
        order.setDeliveryDistrict(deliveryDistrict);
        order.setCustomerName(customerName != null && !customerName.isBlank() ? customerName.trim() : "Client");
        order.setCustomerPhone(customerPhone != null ? customerPhone.trim() : "");
        order.setNotes(notes);
        order.setUser(user);
        order.setCreatedDate(Instant.now());

        BoutiqueOrder savedOrder = boutiqueOrderRepository.save(order);
        for (OrderItem oi : orderItems) {
            oi.setOrder(savedOrder);
            orderItemRepository.save(oi);
        }
        savedOrder.setItemses(new java.util.HashSet<>(orderItems));

        return new BuiltOrder(savedOrder, summary.toString());
    }

    private String generateOrderNumber() {
        for (int attempt = 0; attempt < 6; attempt++) {
            String candidate = "CMD-2026-" + (1000 + RANDOM.nextInt(9000));
            if (boutiqueOrderRepository.findByOrderNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        return "CMD-2026-" + (System.currentTimeMillis() % 1000000);
    }

    private String buildWhatsAppUrl(BoutiqueOrder order, String summaryText) {
        String whatsappPhone = applicationProperties.getBusiness().getWhatsappNumber();
        String messageTemplate = String.format(
            "👋 Bonjour Fotolou ! Je souhaite confirmer ma commande *#%s* :%n%n" +
                "📦 *Articles :*%n%s%n" +
                "💰 *Sous-total :* %,d FCFA%n" +
                "🛵 *Livraison :* %,d FCFA%n" +
                "💵 *TOTAL :* %,d FCFA%n%n" +
                "📍 *Adresse de livraison :* %s (%s)%n" +
                "👤 *Nom :* %s%n" +
                "📞 *Téléphone :* %s",
            order.getOrderNumber(),
            summaryText,
            order.getSubtotal(),
            order.getDeliveryFee(),
            order.getTotalPrice(),
            order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "À préciser",
            order.getDeliveryDistrict() != null ? order.getDeliveryDistrict() : "Dakar",
            order.getCustomerName(),
            order.getCustomerPhone()
        );
        return "https://wa.me/" + whatsappPhone + "?text=" + URLEncoder.encode(messageTemplate, StandardCharsets.UTF_8);
    }

    private OrderStatus parseStatus(String raw, OrderStatus fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return OrderStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private BoutiqueOrderDTO toDto(BoutiqueOrder order) {
        BoutiqueOrderDTO dto = boutiqueOrderMapper.toDto(order);
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            List<OrderItem> lines = orderItemRepository.findByOrderId(order.getId());
            dto.setItems(
                lines
                    .stream()
                    .sorted(java.util.Comparator.comparing(i -> i.getId() == null ? Long.MAX_VALUE : i.getId()))
                    .map(BoutiqueOrderMapper::toOrderLine)
                    .toList()
            );
        }
        return dto;
    }

    private void notifyStatusChange(BoutiqueOrder order) {
        if (order.getUser() == null) {
            return;
        }
        String statusLabel = switch (order.getStatus()) {
            case EN_ATTENTE -> "en attente de confirmation";
            case EN_COURS -> "confirmée et en préparation ✅";
            case LIVRE -> "livrée avec succès 🎉";
            case ANNULE -> "annulée";
        };
        sendInAppNotification(
            order.getUser(),
            RecipientRole.CLIENT,
            NotificationType.ORDER,
            "Suivi Commande 📦",
            String.format("Votre commande #%s est désormais %s.", order.getOrderNumber(), statusLabel),
            "/client/boutique/commandes"
        );
    }

    private void sendInAppNotification(
        User user,
        RecipientRole role,
        NotificationType type,
        String title,
        String message,
        String targetRoute
    ) {
        try {
            AppNotification notif = new AppNotification();
            notif.setUser(user);
            notif.setRecipientRole(role);
            notif.setType(type);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setIsRead(false);
            notif.setTargetRoute(targetRoute);
            notif.setCreatedDate(Instant.now());
            AppNotification saved = appNotificationRepository.save(notif);
            realtimeEventService.broadcast("NOTIFICATION_CREATED", appNotificationMapper.toDto(saved));
            browserPushService.sendToUser(user, saved);
            LOG.info("🔔 Notification In-App commande créée : {}", title);
        } catch (Exception e) {
            LOG.warn("⚠️ Erreur création notification in-app commande : {}", e.getMessage());
        }
    }
}
