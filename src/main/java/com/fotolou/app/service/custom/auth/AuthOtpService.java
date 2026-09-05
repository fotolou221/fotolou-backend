package com.fotolou.app.service.custom.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface de contrat pour le service d'authentification par OTP et émission de JWT.
 */
public interface AuthOtpService {
    record SendOtpResult(String phone, int expiresInSeconds, int resendCooldownSeconds, String message) {}

    record AuthUserProfile(
        Long id,
        String name,
        String phone,
        String role,
        String homeRoute,
        String avatarUrl,
        Long salonId,
        String salonSlug
    ) {}

    record AuthResult(
        @JsonProperty("id_token") String idToken,
        @JsonProperty("token") String token,
        @JsonProperty("refresh_token") String refreshToken,
        AuthUserProfile user
    ) {}

    /**
     * Traite la demande d'envoi d'OTP pour un numéro de téléphone donné.
     */
    SendOtpResult sendOtp(String rawPhone, String role);

    /**
     * Valide le code OTP, connecte ou inscrit l'utilisateur et émet le jeton JWT.
     */
    AuthResult verifyOtp(String rawPhone, String code, String role, String fullName);

    /**
     * Valide un Refresh Token (valide 45 jours) et émet un nouvel Access Token (valide 15 minutes).
     */
    AuthResult refreshToken(String refreshToken);
}
