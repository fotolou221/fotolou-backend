package com.fotolou.app.service.custom.realtime.impl;

import com.fotolou.app.service.custom.realtime.RealtimeEventService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Implémentation du service de diffusion temps réel par Server-Sent Events (SSE).
 */
@Service
public class RealtimeEventServiceImpl implements RealtimeEventService {

    private static final Logger LOG = LoggerFactory.getLogger(RealtimeEventServiceImpl.class);
    private static final Long SSE_TIMEOUT = 24 * 60 * 60 * 1000L; // 24 heures

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        emitters.add(emitter);
        LOG.debug("🔌 Nouveau client temps réel connecté. Clients actifs : {}", emitters.size());

        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data(Map.of("message", "Connexion temps réel Fotolou établie avec succès.")));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    @Override
    public void broadcast(String eventType, Object payload) {
        if (emitters.isEmpty()) {
            return;
        }

        LOG.info("⚡ [REALTIME BROADCAST] Événement '{}' diffusé à {} clients connectés", eventType, emitters.size());
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(payload));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            LOG.debug("🧹 Nettoyage de {} connexions expirées.", deadEmitters.size());
        }
    }

    /**
     * Heartbeat régulier toutes les 15 secondes pour maintenir la connexion active à travers les proxies et routeurs.
     */
    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("ping").name("PING").data("keep-alive"));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}
