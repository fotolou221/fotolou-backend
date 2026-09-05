package com.fotolou.app.web.rest.custom;

import com.fotolou.app.service.custom.auth.AuthOtpService;
import com.fotolou.app.service.custom.auth.AuthOtpService.AuthResult;
import com.fotolou.app.service.custom.auth.AuthOtpService.SendOtpResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification par numéro de téléphone et code OTP.
 */
@Tag(name = "1. Authentification & OTP", description = "Endpoints d'envoi et de vérification des codes OTP par SMS")
@RestController
@RequestMapping("/api/auth")
public class AuthOtpResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthOtpResource.class);

    private final AuthOtpService authOtpService;

    public AuthOtpResource(AuthOtpService authOtpService) {
        this.authOtpService = authOtpService;
    }

    /**
     * DTO de demande d'envoi de code OTP.
     */
    public record SendOtpRequestVM(@NotBlank String phone, String role) {}

    /**
     * DTO de vérification de code OTP.
     */
    public record VerifyOtpRequestVM(@NotBlank String phone, @NotBlank String code, String role, String fullName) {}

    /**
     * POST /api/auth/otp/send : Génère et envoie un code OTP par SMS.
     */
    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequestVM request) {
        try {
            SendOtpResult result = authOtpService.sendOtp(request.phone(), request.role());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Collections.singletonMap("error", "Impossible d'envoyer le code SMS pour le moment.")
            );
        }
    }

    /**
     * POST /api/auth/otp/verify : Vérifie le code OTP et connecte l'utilisateur.
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequestVM request) {
        try {
            AuthResult result = authOtpService.verifyOtp(request.phone(), request.code(), request.role(), request.fullName());
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(result.token());
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Erreur lors de la vérification OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Collections.singletonMap("error", "Une erreur est survenue lors de la vérification du code.")
            );
        }
    }

    /**
     * DTO de rafraîchissement de jeton d'authentification.
     */
    public record RefreshTokenRequestVM(@NotBlank @com.fasterxml.jackson.annotation.JsonProperty("refresh_token") String refreshToken) {
        @com.fasterxml.jackson.annotation.JsonCreator
        public RefreshTokenRequestVM(
            @com.fasterxml.jackson.annotation.JsonProperty("refresh_token") String refreshToken,
            @com.fasterxml.jackson.annotation.JsonProperty("refreshToken") String altRefreshToken
        ) {
            this(refreshToken != null && !refreshToken.isBlank() ? refreshToken : altRefreshToken);
        }
    }

    /**
     * POST /api/auth/refresh : Émet un nouvel Access Token (15 min) à partir du Refresh Token (45 jours) sans SMS.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestVM request) {
        try {
            AuthResult result = authOtpService.refreshToken(request.refreshToken());
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(result.token());
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            LOG.error("Erreur lors du rafraîchissement du jeton", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Collections.singletonMap("error", "Jeton de rafraîchissement invalide ou expiré.")
            );
        }
    }
}
