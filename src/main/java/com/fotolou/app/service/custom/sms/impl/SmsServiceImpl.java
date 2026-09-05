package com.fotolou.app.service.custom.sms.impl;

import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.service.custom.sms.MockSmsProvider;
import com.fotolou.app.service.custom.sms.SmsProvider;
import com.fotolou.app.service.custom.sms.SmsService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service central d'expédition des SMS.
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final ApplicationProperties applicationProperties;
    private final List<SmsProvider> smsProviders;

    public SmsServiceImpl(ApplicationProperties applicationProperties, List<SmsProvider> smsProviders) {
        this.applicationProperties = applicationProperties;
        this.smsProviders = smsProviders;
    }

    @Override
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

    @Override
    public boolean sendOtpCode(String toPhoneNumber, String code) {
        String message = String.format("Fotolou : Votre code de connexion sécurisé est %s. Ne le partagez pas.", code);
        return sendSms(toPhoneNumber, message);
    }

    @Override
    public boolean sendTicketYourTurnAlert(String toPhoneNumber, String salonName, int ticketNumber) {
        String message = String.format(
            "Fotolou : C'est bientôt votre tour chez %s (Ticket #%d) ! Merci de vous présenter au salon.",
            salonName,
            ticketNumber
        );
        return sendSms(toPhoneNumber, message);
    }
}
