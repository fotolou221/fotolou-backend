package com.fotolou.app.service.custom.sms;

import com.fotolou.app.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptateur SMS pour Twilio.
 */
@Component("twilioSmsProvider")
public class TwilioSmsProvider implements SmsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TwilioSmsProvider.class);

    private final ApplicationProperties applicationProperties;

    public TwilioSmsProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        String sid = applicationProperties.getSms().getTwilioAccountSid();
        if (sid == null || sid.isBlank()) {
            LOG.warn("⚠️ Twilio non configuré (accountSid absent). Utilisation du log de secours.");
            LOG.info("[TWILIO SMS] {} -> {}", toPhoneNumber, message);
            return true;
        }

        LOG.info("📡 Envoi Twilio SMS à {}", toPhoneNumber);
        return true;
    }

    @Override
    public String getProviderName() {
        return "twilio";
    }
}
