package com.fotolou.app.service.custom.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fournisseur SMS de développement qui enregistre les messages dans les logs console.
 */
@Component("mockSmsProvider")
public class MockSmsProvider implements SmsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MockSmsProvider.class);

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        LOG.info("================================================================================");
        LOG.info("📱 [MOCK SMS ENVOYÉ] Destinataire: {}", toPhoneNumber);
        LOG.info("💬 Message: {}", message);
        LOG.info("================================================================================");
        return true;
    }

    @Override
    public String getProviderName() {
        return "mock";
    }
}
