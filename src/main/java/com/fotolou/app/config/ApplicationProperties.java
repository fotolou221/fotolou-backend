package com.fotolou.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Fotolou Backend.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Liquibase liquibase = new Liquibase();
    private final Sms sms = new Sms();
    private final Storage storage = new Storage();
    private final Otp otp = new Otp();
    private final Business business = new Business();

    public Liquibase getLiquibase() {
        return liquibase;
    }

    public Sms getSms() {
        return sms;
    }

    public Storage getStorage() {
        return storage;
    }

    public Otp getOtp() {
        return otp;
    }

    public Business getBusiness() {
        return business;
    }

    public static class Liquibase {

        private Boolean asyncStart = true;

        public Boolean getAsyncStart() {
            return asyncStart;
        }

        public void setAsyncStart(Boolean asyncStart) {
            this.asyncStart = asyncStart;
        }
    }

    public static class Sms {

        private String provider = "mock";
        private String senderName = "Fotolou";
        private String orangeClientId;
        private String orangeClientSecret;
        private String twilioAccountSid;
        private String twilioAuthToken;
        private String twilioPhoneNumber;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getOrangeClientId() {
            return orangeClientId;
        }

        public void setOrangeClientId(String orangeClientId) {
            this.orangeClientId = orangeClientId;
        }

        public String getOrangeClientSecret() {
            return orangeClientSecret;
        }

        public void setOrangeClientSecret(String orangeClientSecret) {
            this.orangeClientSecret = orangeClientSecret;
        }

        public String getTwilioAccountSid() {
            return twilioAccountSid;
        }

        public void setTwilioAccountSid(String twilioAccountSid) {
            this.twilioAccountSid = twilioAccountSid;
        }

        public String getTwilioAuthToken() {
            return twilioAuthToken;
        }

        public void setTwilioAuthToken(String twilioAuthToken) {
            this.twilioAuthToken = twilioAuthToken;
        }

        public String getTwilioPhoneNumber() {
            return twilioPhoneNumber;
        }

        public void setTwilioPhoneNumber(String twilioPhoneNumber) {
            this.twilioPhoneNumber = twilioPhoneNumber;
        }
    }

    public static class Storage {

        private String provider = "local";
        private String uploadDir = "uploads";
        private String cdnUrl = "/api/files/";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }

        public String getCdnUrl() {
            return cdnUrl;
        }

        public void setCdnUrl(String cdnUrl) {
            this.cdnUrl = cdnUrl;
        }
    }

    public static class Otp {

        private int expirationSeconds = 300;
        private int maxAttempts = 3;
        private int resendCooldownSeconds = 45;

        public int getExpirationSeconds() {
            return expirationSeconds;
        }

        public void setExpirationSeconds(int expirationSeconds) {
            this.expirationSeconds = expirationSeconds;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getResendCooldownSeconds() {
            return resendCooldownSeconds;
        }

        public void setResendCooldownSeconds(int resendCooldownSeconds) {
            this.resendCooldownSeconds = resendCooldownSeconds;
        }
    }

    public static class Business {

        private String whatsappNumber = "221778627052";
        private long deliveryFee = 2000L;

        public String getWhatsappNumber() {
            return whatsappNumber;
        }

        public void setWhatsappNumber(String whatsappNumber) {
            this.whatsappNumber = whatsappNumber;
        }

        public long getDeliveryFee() {
            return deliveryFee;
        }

        public void setDeliveryFee(long deliveryFee) {
            this.deliveryFee = deliveryFee;
        }
    }
}
