package com.fotolou.app.service.custom.realtime;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Service de diffusion d'événements en temps réel (Server-Sent Events)
 * pour synchroniser instantanément l'ensemble des écrans (Admin, Coiffeurs, Clients).
 */
public interface RealtimeEventService {
    /**
     * Enregistre un client connecté (navigateur client, coiffeur, admin) pour recevoir le flux SSE.
     */
    SseEmitter registerClient();

    /**
     * Diffuse un événement métier à l'ensemble des clients connectés en temps réel.
     *
     * @param eventType Nom de l'événement (ex: SALON_CREATED, SALON_UPDATED, TICKET_UPDATED, QUEUE_UPDATED)
     * @param payload Données sérialisées de l'événement
     */
    void broadcast(String eventType, Object payload);
}
