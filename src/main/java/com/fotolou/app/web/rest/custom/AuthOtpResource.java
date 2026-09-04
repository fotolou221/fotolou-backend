package com.fotolou.app.web.rest.custom;

import static com.fotolou.app.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.fotolou.app.security.SecurityUtils.JWT_ALGORITHM;
import static com.fotolou.app.security.SecurityUtils.USER_ID_CLAIM;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.Authority;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.AuthorityRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.AuthoritiesConstants;
import com.fotolou.app.service.custom.otp.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification par numéro de téléphone et code OTP.
 */
@Tag(name = "1. Authentification & OTP", description = "Endpoints d'envoi et de vérification des codes OTP par SMS")
@RestController
@RequestMapping("/api/auth")
public class AuthOtpResource {

    private static final Logger LOG = LoggerFactory.getLogger(AuthOtpResource.class);

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final ApplicationProperties applicationProperties;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:86400}")
    private long tokenValidityInSeconds;

    public AuthOtpResource(
        OtpService otpService,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder,
        JwtEncoder jwtEncoder,
        ApplicationProperties applicationProperties
    ) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.applicationProperties = applicationProperties;
    }

    /**
     * DTO de demande d'envoi de code OTP.
     */
    public record SendOtpRequestVM(@NotBlank String phone, String role) {}

    /**
     * DTO de réponse pour l'envoi OTP.
     */
    public record SendOtpResponseVM(String phone, int expiresInSeconds, int resendCooldownSeconds, String message) {}

    /**
     * DTO de vérification de code OTP.
     */
    public record VerifyOtpRequestVM(@NotBlank String phone, @NotBlank String code, String role, String fullName) {}

    /**
     * DTO du profil utilisateur retourné à la connexion.
     */
    public record AuthUserProfileDTO(Long id, String name, String phone, String role, String homeRoute, String avatarUrl) {}

    /**
     * DTO de réponse à la vérification OTP avec JWT token.
     */
    public record AuthResponseVM(@JsonProperty("id_token") String idToken, @JsonProperty("token") String token, AuthUserProfileDTO user) {}

    /**
     * POST /api/auth/otp/send : Génère et envoie un code OTP par SMS.
     */
    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequestVM request) {
        try {
            String normalizedPhone = otpService.normalizePhoneNumber(request.phone());
            otpService.generateAndSendOtp(normalizedPhone);

            int expiration = applicationProperties.getOtp().getExpirationSeconds();
            int cooldown = applicationProperties.getOtp().getResendCooldownSeconds();

            return ResponseEntity.ok(
                new SendOtpResponseVM(normalizedPhone, expiration, cooldown, "Code de vérification envoyé avec succès par SMS.")
            );
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
        String normalizedPhone = otpService.normalizePhoneNumber(request.phone());
        boolean isValid = otpService.verifyOtp(normalizedPhone, request.code());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Collections.singletonMap("error", "Code de vérification invalide ou expiré.")
            );
        }

        // Récupération ou création automatique du compte User
        String targetRole =
            request.role() != null && request.role().equalsIgnoreCase("coiffeur")
                ? AuthoritiesConstants.COIFFEUR
                : AuthoritiesConstants.CLIENT;

        User user = findOrCreateUser(normalizedPhone, targetRole, request.fullName());

        // Génération du JWT
        String authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.joining(" "));
        Instant now = Instant.now();
        Instant validity = now.plus(tokenValidityInSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(user.getLogin())
            .claim(AUTHORITIES_CLAIM, authorities)
            .claim(USER_ID_CLAIM, user.getId())
            .claim("role", targetRole.replace("ROLE_", "").toLowerCase())
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        String jwtToken = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        boolean isCoiffeur = user
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getName().equals(AuthoritiesConstants.COIFFEUR));
        boolean isAdmin = user
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getName().equals(AuthoritiesConstants.ADMIN) || a.getName().equals(AuthoritiesConstants.SUPER_ADMIN));

        String roleClean = isAdmin ? "admin" : isCoiffeur ? "coiffeur" : "client";
        String homeRoute = isAdmin ? "/admin/dashboard" : isCoiffeur ? "/coiffeur/home" : "/client/home";

        String displayName =
            user.getFirstName() != null && !user.getFirstName().isBlank()
                ? user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")
                : isCoiffeur
                  ? "Barbier Fotolou"
                  : isAdmin
                    ? "Administrateur Fotolou"
                    : "Client Fotolou";

        AuthUserProfileDTO profile = new AuthUserProfileDTO(
            user.getId(),
            displayName,
            user.getLogin(),
            roleClean,
            homeRoute,
            user.getImageUrl()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        return new ResponseEntity<>(new AuthResponseVM(jwtToken, jwtToken, profile), headers, HttpStatus.OK);
    }

    private User findOrCreateUser(String phone, String roleName, String optionalName) {
        Optional<User> optUser = userRepository.findOneWithAuthoritiesByLogin(phone);
        if (optUser.isPresent()) {
            return optUser.get();
        }

        User newUser = new User();
        newUser.setLogin(phone);
        newUser.setPassword(passwordEncoder.encode(phone + "_fotolou_secret_key"));
        newUser.setActivated(true);
        newUser.setLangKey("fr");

        if (optionalName != null && !optionalName.isBlank()) {
            String[] parts = optionalName.trim().split(" ", 2);
            newUser.setFirstName(parts[0]);
            if (parts.length > 1) {
                newUser.setLastName(parts[1]);
            }
        } else {
            newUser.setFirstName(roleName.equals(AuthoritiesConstants.COIFFEUR) ? "Coiffeur" : "Client");
            newUser.setLastName(phone.substring(Math.max(0, phone.length() - 4)));
        }

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(roleName).ifPresent(authorities::add);
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        return userRepository.save(newUser);
    }
}
