package com.fotolou.app.service.custom.otp;

/**
 * Interface du service gérant le cycle de vie des codes OTP.
 */
public interface OtpService {
    /**
     * Nettoie et normalise le numéro de téléphone au format sénégalais/international.
     */
    String normalizePhoneNumber(String rawPhone);

    /**
     * Génère et expédie un code OTP par SMS.
     */
    boolean generateAndSendOtp(String rawPhone);

    /**
     * Valide un code OTP saisi par l'utilisateur.
     */
    boolean verifyOtp(String rawPhone, String inputCode);
}
