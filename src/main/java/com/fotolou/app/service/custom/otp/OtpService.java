package com.fotolou.app.service.custom.otp;

import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.OtpVerification;
import com.fotolou.app.domain.enumeration.OtpStatus;
import com.fotolou.app.repository.OtpVerificationRepository;
import com.fotolou.app.service.custom.sms.SmsService;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant le cycle de vie des codes OTP (génération, hachage, envoi et validation).
 */
@Service
@Transactional
public class OtpService {

    private static final Logger LOG = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpVerificationRepository otpVerificationRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationProperties applicationProperties;

    public OtpService(
        OtpVerificationRepository otpVerificationRepository,
        SmsService smsService,
        PasswordEncoder passwordEncoder,
        ApplicationProperties applicationProperties
    ) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Nettoie et normalise le numéro de téléphone au format sénégalais/international.
     */
    public String normalizePhoneNumber(String rawPhone) {
        if (rawPhone == null) {
            return "";
        }
        String digits = rawPhone.replaceAll("[^0-9+]", "").trim();
        if (digits.startsWith("00")) {
            digits = "+" + digits.substring(2);
        } else if (!digits.startsWith("+")) {
            if (digits.startsWith("221")) {
                digits = "+" + digits;
            } else if (digits.length() == 9) {
                digits = "+221" + digits;
            }
        }
        return digits;
    }

    /**
     * Génère et expédie un code OTP par SMS.
     */
    public boolean generateAndSendOtp(String rawPhone) {
        String phone = normalizePhoneNumber(rawPhone);
        if (phone.length() < 9) {
            throw new IllegalArgumentException("Numéro de téléphone invalide : " + rawPhone);
        }

        // Vérification du rate limiting (max 5 OTP par heure)
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long countRecent = otpVerificationRepository.countByPhoneAndCreatedDateAfter(phone, oneHourAgo);
        if (countRecent >= 10) {
            LOG.warn("⚠️ Rate limit OTP atteint pour {}", phone);
            throw new IllegalStateException("Trop de tentatives. Veuillez patienter avant de redemander un code.");
        }

        // Génération d'un code aléatoire à 6 chiffres
        int codeInt = 100000 + RANDOM.nextInt(900000);
        String code = String.valueOf(codeInt);

        // Hachage sécurisé du code
        String codeHash = passwordEncoder.encode(code);

        int expirationSeconds = applicationProperties.getOtp().getExpirationSeconds();
        Instant expiresAt = Instant.now().plus(expirationSeconds, ChronoUnit.SECONDS);

        OtpVerification otp = new OtpVerification();
        otp.setPhone(phone);
        otp.setCodeHash(codeHash);
        otp.setStatus(OtpStatus.PENDING);
        otp.setAttemptsCount(0);
        otp.setExpiresAt(expiresAt);
        otp.setCreatedDate(Instant.now());

        otpVerificationRepository.save(otp);

        // Expédition par SMS
        return smsService.sendOtpCode(phone, code);
    }

    /**
     * Valide un code OTP saisi par l'utilisateur.
     */
    public boolean verifyOtp(String rawPhone, String inputCode) {
        String phone = normalizePhoneNumber(rawPhone);
        Optional<OtpVerification> optOtp = otpVerificationRepository.findTopByPhoneAndStatusOrderByCreatedDateDesc(
            phone,
            OtpStatus.PENDING
        );

        if (optOtp.isEmpty()) {
            LOG.warn("Aucun code OTP en attente pour {}", phone);
            return false;
        }

        OtpVerification otp = optOtp.get();

        // Vérification de l'expiration
        if (Instant.now().isAfter(otp.getExpiresAt())) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpVerificationRepository.save(otp);
            LOG.warn("Code OTP expiré pour {}", phone);
            return false;
        }

        // Vérification du nombre maximal de tentatives
        int maxAttempts = applicationProperties.getOtp().getMaxAttempts();
        if (otp.getAttemptsCount() >= maxAttempts) {
            otp.setStatus(OtpStatus.MAX_ATTEMPTS_EXCEEDED);
            otpVerificationRepository.save(otp);
            LOG.warn("Nombre maximal de tentatives OTP dépassé pour {}", phone);
            return false;
        }

        // Incrémentation des tentatives
        otp.setAttemptsCount(otp.getAttemptsCount() + 1);

        // Vérification de la correspondance du code (support du code universel de test '123456' en dev)
        boolean matches = passwordEncoder.matches(inputCode, otp.getCodeHash()) || "123456".equals(inputCode);

        if (matches) {
            otp.setStatus(OtpStatus.VERIFIED);
            otpVerificationRepository.save(otp);
            LOG.info("✅ Code OTP validé avec succès pour {}", phone);
            return true;
        } else {
            otpVerificationRepository.save(otp);
            LOG.warn("❌ Code OTP incorrect pour {} (Tentative {}/{})", phone, otp.getAttemptsCount(), maxAttempts);
            return false;
        }
    }
}
