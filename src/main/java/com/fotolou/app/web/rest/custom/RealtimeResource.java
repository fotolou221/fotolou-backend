package com.fotolou.app.web.rest.custom;

import com.fotolou.app.service.custom.realtime.RealtimeEventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Contrôleur REST pour le flux d'événements temps réel SSE (Server-Sent Events).
 */
@Tag(name = "10. Synchronisation Temps Réel", description = "Flux Server-Sent Events pour la synchronisation instantanée")
@RestController
@RequestMapping("/api")
public class RealtimeResource {

    private final RealtimeEventService realtimeEventService;

    public RealtimeResource(RealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    /**
     * GET /api/realtime/events : Flux SSE pour recevoir en temps réel les changements de salons, files et tickets.
     */
    @GetMapping(value = "/realtime/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return realtimeEventService.registerClient();
    }
}
