package com.fotolou.app.service.custom.sms;

import com.fotolou.app.config.ApplicationProperties;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptateur SMS pour Twilio via l'API REST officielle Twilio (compatible Twilio Cloud).
 */
@Component("twilioSmsProvider")
public class TwilioSmsProvider implements SmsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TwilioSmsProvider.class);

    private final ApplicationProperties applicationProperties;
    private final HttpClient httpClient;

    public TwilioSmsProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public boolean sendSms(String toPhoneNumber, String message) {
        String sid = applicationProperties.getSms().getTwilioAccountSid();
        String authToken = applicationProperties.getSms().getTwilioAuthToken();
        String fromNumber = applicationProperties.getSms().getTwilioPhoneNumber();

        // Si non configuré, mode simulation / log de secours
        if (sid == null || sid.isBlank() || authToken == null || authToken.isBlank() || fromNumber == null || fromNumber.isBlank()) {
            LOG.warn("⚠️ Twilio non entièrement configuré (Account SID, Auth Token ou Phone Number manquant). Mode simulation actif.");
            LOG.info("[TWILIO SMS SIMULATION] {} -> {}", toPhoneNumber, message);
            return true;
        }

        try {
            String sanitizedTo = toPhoneNumber.trim().replaceAll("\\s+", "");
            if (!sanitizedTo.startsWith("+")) {
                sanitizedTo = "+221" + sanitizedTo; // Format international indicatif Sénégal par défaut
            }

            String url = "https://api.twilio.com/2010-04-01/Accounts/" + sid.trim() + "/Messages.json";
            String credentials = sid.trim() + ":" + authToken.trim();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            String formBody =
                "To=" +
                URLEncoder.encode(sanitizedTo, StandardCharsets.UTF_8) +
                "&From=" +
                URLEncoder.encode(fromNumber.trim(), StandardCharsets.UTF_8) +
                "&Body=" +
                URLEncoder.encode(message, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.info("✅ SMS Twilio envoyé avec succès à {} (HTTP {})", sanitizedTo, response.statusCode());
                return true;
            } else {
                LOG.error("❌ Échec envoi SMS Twilio vers {} (HTTP {}) : {}", sanitizedTo, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            LOG.error("❌ Exception lors de l'envoi SMS Twilio vers {} : {}", toPhoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "twilio";
    }
}
