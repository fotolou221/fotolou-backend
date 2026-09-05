package com.fotolou.app.config;

import static com.fotolou.app.security.SecurityUtils.JWT_ALGORITHM;

import com.fotolou.app.management.SecurityMetersService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

@Configuration
public class SecurityJwtConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityJwtConfiguration.class);

    @Value(
        "${jhipster.security.authentication.jwt.base64-secret:${JWT_SECRET:YjhhZjkyNGU5MGM5NTFiMzQ1MTNkNWJhYzExMWRjOGE1Yzg4ZTE1NGRjNTY5ZjkyMjg0ZjUzNGNiMzI3MGY4ODlhNDdlMzgxYTk4MmViMGViMGVhYWRiYjA3MDdmMjU1MTcxN2Q5NTFhMGMyOGYwYzFjNWU4MzE2YmQwZTk4YTY=}}"
    )
    private String jwtKey;

    private static final String DEFAULT_FALLBACK_SECRET =
        "YjhhZjkyNGU5MGM5NTFiMzQ1MTNkNWJhYzExMWRjOGE1Yzg4ZTE1NGRjNTY5ZjkyMjg0ZjUzNGNiMzI3MGY4ODlhNDdlMzgxYTk4MmViMGViMGVhYWRiYjA3MDdmMjU1MTcxN2Q5NTFhMGMyOGYwYzFjNWU4MzE2YmQwZTk4YTY=";

    @Bean
    public JwtDecoder jwtDecoder(SecurityMetersService metersService) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(JWT_ALGORITHM).build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                if (e.getMessage().contains("Invalid signature")) {
                    metersService.trackTokenInvalidSignature();
                } else if (e.getMessage().contains("Jwt expired at")) {
                    metersService.trackTokenExpired();
                } else if (
                    e.getMessage().contains("Invalid JWT serialization") ||
                    e.getMessage().contains("Malformed token") ||
                    e.getMessage().contains("Invalid unsecured/JWS/JWE")
                ) {
                    metersService.trackTokenMalformed();
                } else {
                    LOG.error("Unknown JWT error {}", e.getMessage());
                }
                throw e;
            }
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        var bearerTokenResolver = new DefaultBearerTokenResolver();
        bearerTokenResolver.setAllowUriQueryParameter(true);
        return bearerTokenResolver;
    }

    private SecretKey getSecretKey() {
        String key = jwtKey != null && !jwtKey.isBlank() ? jwtKey.trim() : DEFAULT_FALLBACK_SECRET;
        byte[] keyBytes = Base64.from(key).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
