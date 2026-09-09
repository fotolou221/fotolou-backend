package com.fotolou.app.service.custom.push;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.AppNotification;
import com.fotolou.app.domain.PushSubscription;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.PushSubscriptionRepository;
import java.security.Security;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrowserPushService {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserPushService.class);

    private final ApplicationProperties applicationProperties;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    public BrowserPushService(
        ApplicationProperties applicationProperties,
        PushSubscriptionRepository pushSubscriptionRepository,
        ObjectMapper objectMapper
    ) {
        this.applicationProperties = applicationProperties;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
    }

    public String getPublicKey() {
        return clean(applicationProperties.getPush().getVapidPublicKey());
    }

    public boolean isConfigured() {
        return getPublicKey() != null && clean(applicationProperties.getPush().getVapidPrivateKey()) != null;
    }

    public void registerSubscription(User user, PushSubscriptionRequest request) {
        if (user == null || user.getId() == null || request == null || request.keys() == null) {
            return;
        }

        Instant now = Instant.now();
        PushSubscription subscription = pushSubscriptionRepository.findByEndpoint(request.endpoint()).orElseGet(() -> {
            PushSubscription created = new PushSubscription();
            created.setEndpoint(request.endpoint());
            created.setCreatedDate(now);
            return created;
        });

        subscription.setUser(user);
        subscription.setP256dh(request.keys().p256dh());
        subscription.setAuth(request.keys().auth());
        subscription.setLastSeenDate(now);
        pushSubscriptionRepository.save(subscription);
    }

    @Async
    public void sendToUser(User user, AppNotification appNotification) {
        if (!isConfigured() || user == null || user.getId() == null || appNotification == null) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(user.getId());
        if (subscriptions.isEmpty()) {
            return;
        }

        ensureBouncyCastleProvider();
        String privateKey = clean(applicationProperties.getPush().getVapidPrivateKey());
        String subject = clean(applicationProperties.getPush().getSubject());
        if (subject == null) {
            subject = "mailto:support@fotolou.sn";
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(toPushPayload(appNotification));
        } catch (JsonProcessingException e) {
            LOG.warn("Unable to serialize push notification {}: {}", appNotification.getId(), e.getMessage());
            return;
        }

        for (PushSubscription subscription : subscriptions) {
            try {
                PushService pushService = new PushService(getPublicKey(), privateKey, subject);
                Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload
                );
                HttpResponse response = pushService.send(notification);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 404 || statusCode == 410) {
                    pushSubscriptionRepository.delete(subscription);
                } else if (statusCode >= 400) {
                    LOG.warn("Browser refused push notification (HTTP {})", statusCode);
                }
            } catch (Exception e) {
                LOG.warn("Push notification send error {}: {}", appNotification.getId(), e.getMessage());
            }
        }
    }

    private Map<String, Object> toPushPayload(AppNotification appNotification) {
        String targetRoute = clean(appNotification.getTargetRoute());
        if (targetRoute == null) {
            targetRoute = "/client/notifications";
        }

        Map<String, Object> defaultClick = new LinkedHashMap<>();
        defaultClick.put("operation", "openWindow");
        defaultClick.put("url", targetRoute);

        Map<String, Object> onActionClick = new LinkedHashMap<>();
        onActionClick.put("default", defaultClick);
        onActionClick.put("open", defaultClick);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", targetRoute);
        data.put("notificationId", appNotification.getId());
        data.put("onActionClick", onActionClick);

        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("title", appNotification.getTitle());
        notification.put("body", appNotification.getMessage());
        notification.put("icon", "/icons/icon-192x192.png");
        notification.put("badge", "/icons/favicon-64.png");
        notification.put("tag", "fotolou-notification-" + appNotification.getId());
        notification.put("renotify", true);
        notification.put("data", data);
        notification.put("actions", List.of(Map.of("action", "open", "title", "Ouvrir")));

        return Map.of("notification", notification);
    }

    private void ensureBouncyCastleProvider() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
