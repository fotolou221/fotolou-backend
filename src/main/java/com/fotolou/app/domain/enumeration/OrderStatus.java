package com.fotolou.app.domain.enumeration;

/**
 * The OrderStatus enumeration.
 *
 * EN_ATTENTE : commande passée par le client, en attente de confirmation (WhatsApp / appel / admin).
 * EN_COURS   : commande confirmée, en préparation / livraison.
 * LIVRE      : commande livrée.
 * ANNULE     : commande annulée.
 */
public enum OrderStatus {
    EN_ATTENTE,
    EN_COURS,
    LIVRE,
    ANNULE,
}
