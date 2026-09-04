package com.fotolou.app.service.custom.sms;

/**
 * Interface pour les fournisseurs de SMS (Orange Sénégal, Twilio, Mock Dev).
 */
public interface SmsProvider {
    /**
     * Envoie un SMS à un numéro de téléphone.
     *
     * @param toPhoneNumber Numéro de téléphone destinataire (ex: +221778627052)
     * @param message Contenu textuel du message
     * @return true si le message a été transmis avec succès, false sinon
     */
    boolean sendSms(String toPhoneNumber, String message);

    /**
     * Identifiant du provider (ex: "mock", "orange", "twilio").
     */
    String getProviderName();
}
