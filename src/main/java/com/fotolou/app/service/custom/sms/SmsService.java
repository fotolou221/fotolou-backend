package com.fotolou.app.service.custom.sms;

/**
 * Interface du service central d'expédition des SMS.
 */
public interface SmsService {
    /**
     * Expédie un SMS en utilisant le provider configuré.
     */
    boolean sendSms(String toPhoneNumber, String message);

    /**
     * Envoi de SMS de code OTP de connexion.
     */
    boolean sendOtpCode(String toPhoneNumber, String code);

    /**
     * Envoi de SMS d'alerte de tour imminent au salon.
     */
    boolean sendTicketYourTurnAlert(String toPhoneNumber, String salonName, int ticketNumber);
}
