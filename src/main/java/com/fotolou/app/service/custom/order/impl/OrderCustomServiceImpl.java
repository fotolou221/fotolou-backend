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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service métier pour les commandes e-commerce de la boutique Fotolou.
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

    @Override
    public CheckoutResult checkout(CheckoutRequest request, String userLogin) {
        User currentUser = userLogin != null ? userRepository.findOneByLogin(userLogin).orElse(null) : null;

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

        if (currentUser != null) {
            sendInAppNotification(
                currentUser,
                RecipientRole.CLIENT,
                NotificationType.ORDER,
                "Commande validée 🛍️",
                String.format("Votre commande #%s d'un montant de %,d FCFA a été transmise avec succès.", orderNumber, totalPrice),
                "/client/boutique/commandes"
            );
        }

        LOG.info("🛍️ Commande #{} enregistrée avec succès (Total: {} FCFA)", orderNumber, totalPrice);

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

        List<BoutiqueOrder> orders = boutiqueOrderRepository.findByUserIdOrderByCreatedDateDesc(user.getId());
        return orders.stream().map(boutiqueOrderMapper::toDto).toList();
    }

    @Override
    public BoutiqueOrderDTO updateOrderStatus(Long orderId, String status) {
        BoutiqueOrder order = boutiqueOrderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Commande introuvable ID : " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        order.setLastModifiedDate(Instant.now());
        BoutiqueOrder saved = boutiqueOrderRepository.save(order);

        if (saved.getUser() != null) {
            String statusLabel = switch (newStatus) {
                case EN_COURS -> "en cours de préparation";
                case LIVRE -> "livrée avec succès 🎉";
                case ANNULE -> "annulée";
            };
            sendInAppNotification(
                saved.getUser(),
                RecipientRole.CLIENT,
                NotificationType.ORDER,
                "Suivi Commande 📦",
                String.format("Votre commande #%s est désormais %s.", saved.getOrderNumber(), statusLabel),
                "/client/boutique/commandes"
            );
        }

        LOG.info("📦 Statut de la commande #{} changé en {}", order.getOrderNumber(), newStatus);
        return boutiqueOrderMapper.toDto(saved);
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
