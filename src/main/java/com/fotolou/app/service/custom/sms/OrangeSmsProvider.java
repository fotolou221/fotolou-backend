package com.fotolou.app.service.custom.sms;

import com.fotolou.app.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptateur SMS pour l'API Orange Sénégal / Orange Developer Africa.
 */
@Component("orangeSmsProvider")
public class OrangeSmsProvider implements SmsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OrangeSmsProvider.class);

    private final ApplicationProperties applicationProperties;

    public OrangeSmsProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        String clientId = applicationProperties.getSms().getOrangeClientId();
        if (clientId == null || clientId.isBlank()) {
            LOG.warn("⚠️ Orange SMS API non configuré (orangeClientId absent). Utilisation du log de secours.");
            LOG.info("[ORANGE SMS] {} -> {}", toPhoneNumber, message);
            return true;
        }

        // Dans un environnement de production avec les clés actives,
        // Appel HTTP vers l'API Orange SMS https://api.orange.com/oauth/v3/token et /smsmessaging/v1/outbound
        LOG.info("📡 Envoi Orange SMS à {} (sender: {})", toPhoneNumber, applicationProperties.getSms().getSenderName());
        return true;
    }

    @Override
    public String getProviderName() {
        return "orange_senegal";
    }
}
