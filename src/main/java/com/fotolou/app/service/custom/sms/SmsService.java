package com.fotolou.app.service.custom.sms;

import com.fotolou.app.config.ApplicationProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service central d'expédition des SMS et notifications mobiles.
 */
@Service
public class SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    private final ApplicationProperties applicationProperties;
    private final List<SmsProvider> smsProviders;

    public SmsService(ApplicationProperties applicationProperties, List<SmsProvider> smsProviders) {
        this.applicationProperties = applicationProperties;
        this.smsProviders = smsProviders;
    }

    /**
     * Expédie un SMS en utilisant le provider configuré dans application.yml / variables d'environnement.
     */
    public boolean sendSms(String toPhoneNumber, String message) {
        String activeProviderName = applicationProperties.getSms().getProvider();

        SmsProvider provider = smsProviders
            .stream()
            .filter(p -> p.getProviderName().equalsIgnoreCase(activeProviderName))
            .findFirst()
            .orElseGet(() -> {
                LOG.warn("⚠️ Provider '{}' non trouvé, bascule sur mockSmsProvider", activeProviderName);
                return smsProviders
                    .stream()
                    .filter(p -> p.getProviderName().equalsIgnoreCase("mock"))
                    .findFirst()
                    .orElse(new MockSmsProvider());
            });

        return provider.sendSms(toPhoneNumber, message);
    }

    /**
     * Envoi de SMS de code OTP de connexion.
     */
    public boolean sendOtpCode(String toPhoneNumber, String code) {
        String message = String.format("Fotolou : Votre code de connexion sécurisé est %s. Ne le partagez pas.", code);
        return sendSms(toPhoneNumber, message);
    }

    /**
     * Envoi de SMS d'alerte de tour imminent au salon.
     */
    public boolean sendTicketYourTurnAlert(String toPhoneNumber, String salonName, int ticketNumber) {
        String message = String.format(
            "Fotolou : C'est bientôt votre tour chez %s (Ticket #%d) ! Merci de vous présenter au salon.",
            salonName,
            ticketNumber
        );
        return sendSms(toPhoneNumber, message);
    }
}
