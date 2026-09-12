package com.fotolou.app.service.custom.auth.impl;

import static com.fotolou.app.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.fotolou.app.security.SecurityUtils.JWT_ALGORITHM;
import static com.fotolou.app.security.SecurityUtils.TOKEN_TYPE_CLAIM;
import static com.fotolou.app.security.SecurityUtils.USER_ID_CLAIM;

import com.fotolou.app.config.ApplicationProperties;
import com.fotolou.app.domain.Authority;
import com.fotolou.app.domain.User;
import com.fotolou.app.repository.AuthorityRepository;
import com.fotolou.app.repository.UserRepository;
import com.fotolou.app.security.AuthoritiesConstants;
import com.fotolou.app.service.custom.auth.AuthOtpService;
import com.fotolou.app.service.custom.otp.OtpService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service d'authentification OTP et émission de JWT.
 */
@Service
@Transactional
public class AuthOtpServiceImpl implements AuthOtpService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthOtpServiceImpl.class);

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final ApplicationProperties applicationProperties;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:900}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:3888000}")
    private long refreshTokenValidityInSeconds;

    public AuthOtpServiceImpl(
        OtpService otpService,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        com.fotolou.app.repository.CoiffeurProfileRepository coiffeurProfileRepository,
        PasswordEncoder passwordEncoder,
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        ApplicationProperties applicationProperties
    ) {
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.coiffeurProfileRepository = coiffeurProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public SendOtpResult sendOtp(String rawPhone, String role) {
        String normalizedPhone = otpService.normalizePhoneNumber(rawPhone);
        otpService.generateAndSendOtp(normalizedPhone);

        int expiration = applicationProperties.getOtp().getExpirationSeconds();
        int cooldown = applicationProperties.getOtp().getResendCooldownSeconds();

        return new SendOtpResult(normalizedPhone, expiration, cooldown, "Code de vérification envoyé avec succès par SMS.");
    }

    @Override
    public AuthResult verifyOtp(String rawPhone, String code, String role, String fullName) {
        String normalizedPhone = otpService.normalizePhoneNumber(rawPhone);
        boolean isValid = otpService.verifyOtp(normalizedPhone, code);

        if (!isValid) {
            throw new BadCredentialsException("Code de vérification invalide ou expiré.");
        }

        User user = findOrCreateUser(normalizedPhone, fullName);
        AuthUserProfile profile = buildProfile(user, role);
        String accessToken = createAccessToken(user, profile.role());
        String refreshToken = createRefreshToken(user, profile.role());

        return new AuthResult(accessToken, accessToken, refreshToken, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResult refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new BadCredentialsException("Jeton de rafraîchissement absent.");
        }
        try {
            Jwt jwt = this.jwtDecoder.decode(refreshTokenStr);
            String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
            if (!"REFRESH".equals(tokenType)) {
                throw new BadCredentialsException("Type de jeton invalide (attendu: REFRESH).");
            }
            String login = jwt.getSubject();
            if (login == null) {
                throw new BadCredentialsException("Identifiant utilisateur manquant dans le jeton.");
            }

            User user = userRepository
                .findOneWithAuthoritiesByLogin(login)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur associé au jeton introuvable."));

            if (!Boolean.TRUE.equals(user.isActivated())) {
                throw new BadCredentialsException("Le compte utilisateur est désactivé.");
            }

            String requestedRole = jwt.getClaimAsString("role");
            AuthUserProfile profile = buildProfile(user, requestedRole);
            String newAccessToken = createAccessToken(user, profile.role());
            String newRefreshToken = createRefreshToken(user, profile.role());

            LOG.info("🔄 Jeton rafraîchi avec succès pour l'utilisateur : {} (15 min access, 45 jours refresh)", login);
            return new AuthResult(newAccessToken, newAccessToken, newRefreshToken, profile);
        } catch (JwtException e) {
            LOG.warn("Échec du décodage du Refresh Token : {}", e.getMessage());
            throw new BadCredentialsException("Jeton de rafraîchissement invalide ou expiré : " + e.getMessage());
        }
    }

    private String createAccessToken(User user, String roleClean) {
        String authorities = resolveTokenAuthorities(user, roleClean);
        Instant now = Instant.now();
        Instant validity = now.plus(tokenValidityInSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(user.getLogin())
            .claim(AUTHORITIES_CLAIM, authorities)
            .claim(USER_ID_CLAIM, user.getId())
            .claim(TOKEN_TYPE_CLAIM, "ACCESS")
            .claim("role", roleClean)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private String resolveTokenAuthorities(User user, String roleClean) {
        if ("admin".equals(roleClean)) {
            return user.getAuthorities().stream().map(Authority::getName).collect(Collectors.joining(" "));
        }

        String primaryRole = "coiffeur".equals(roleClean) ? AuthoritiesConstants.COIFFEUR : AuthoritiesConstants.CLIENT;
        return AuthoritiesConstants.USER + " " + primaryRole;
    }

    private String createRefreshToken(User user, String roleClean) {
        Instant now = Instant.now();
        Instant validity = now.plus(refreshTokenValidityInSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(user.getLogin())
            .claim(USER_ID_CLAIM, user.getId())
            .claim(TOKEN_TYPE_CLAIM, "REFRESH")
            .claim("role", roleClean)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private AuthUserProfile buildProfile(User user, String requestedRole) {
        Optional<com.fotolou.app.domain.CoiffeurProfile> optProfile = coiffeurProfileRepository.findOneWithSalonByUserLogin(
            user.getLogin()
        );

        if (optProfile.isEmpty()) {
            String rawPhone = user.getPhone() != null && !user.getPhone().isBlank() ? user.getPhone() : user.getLogin();
            if (rawPhone != null && !rawPhone.isBlank()) {
                String digitsOnly = rawPhone.replaceAll("[^0-9]", "");
                String local9 = digitsOnly.length() >= 9 ? digitsOnly.substring(digitsOnly.length() - 9) : digitsOnly;
                String intlPhone = "+221" + local9;

                optProfile = coiffeurProfileRepository
                    .findByPhone(intlPhone)
                    .or(() -> coiffeurProfileRepository.findByPhone(local9))
                    .or(() -> coiffeurProfileRepository.findByPhone(rawPhone));
            }
        }

        if (optProfile.isPresent() && optProfile.get().getUser() == null) {
            optProfile.get().setUser(user);
            try {
                coiffeurProfileRepository.save(optProfile.get());
            } catch (Exception e) {
                LOG.warn("Impossible de lier l'utilisateur au profil coiffeur: {}", e.getMessage());
            }
        }

        Long salonId = null;
        String salonSlug = null;
        if (optProfile.isPresent() && optProfile.get().getSalon() != null) {
            salonId = optProfile.get().getSalon().getId();
            salonSlug = optProfile.get().getSalon().getSlug();
        }

        boolean hasCoiffeurProfile = optProfile.isPresent();
        boolean hasCoiffeurAuthority =
            user.getAuthorities() != null &&
            user
                .getAuthorities()
                .stream()
                .anyMatch(a -> AuthoritiesConstants.COIFFEUR.equalsIgnoreCase(a.getName()) || "COIFFEUR".equalsIgnoreCase(a.getName()));
        boolean isCoiffeur = hasCoiffeurProfile || hasCoiffeurAuthority;

        boolean isAdmin =
            user.getAuthorities() != null &&
            user
                .getAuthorities()
                .stream()
                .anyMatch(
                    a ->
                        AuthoritiesConstants.ADMIN.equalsIgnoreCase(a.getName()) ||
                        AuthoritiesConstants.SUPER_ADMIN.equalsIgnoreCase(a.getName())
                );

        String roleClean;
        if (isAdmin) {
            roleClean = "admin";
        } else if (isCoiffeur) {
            roleClean = "coiffeur";
        } else {
            roleClean = "client";
        }

        if (isCoiffeur && !hasCoiffeurAuthority) {
            authorityRepository.findById(AuthoritiesConstants.COIFFEUR).ifPresent(auth -> {
                Set<Authority> authorities = new HashSet<>(user.getAuthorities());
                authorities.add(auth);
                user.setAuthorities(authorities);
                try {
                    userRepository.save(user);
                } catch (Exception e) {
                    LOG.warn("Impossible d'ajouter le rôle COIFFEUR à l'utilisateur : {}", e.getMessage());
                }
            });
        }

        String homeRoute = switch (roleClean) {
            case "admin" -> "/admin/dashboard";
            case "coiffeur" -> "/coiffeur/home";
            default -> "/client/home";
        };

        String displayName =
            user.getFirstName() != null && !user.getFirstName().isBlank()
                ? user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "")
                : "coiffeur".equals(roleClean)
                  ? optProfile.isPresent() && optProfile.get().getName() != null && !optProfile.get().getName().isBlank()
                      ? optProfile.get().getName()
                      : "Barbier Fotolou"
                  : isAdmin
                    ? "Administrateur Fotolou"
                    : "Client Fotolou";

        return new AuthUserProfile(
            user.getId(),
            displayName,
            user.getLogin(),
            roleClean,
            homeRoute,
            user.getImageUrl(),
            salonId,
            salonSlug
        );
    }

    private User findOrCreateUser(String phone, String optionalName) {
        String digitsOnly = phone != null ? phone.replaceAll("[^0-9]", "") : "";
        String local9 = digitsOnly.length() >= 9 ? digitsOnly.substring(digitsOnly.length() - 9) : digitsOnly;
        String intlPhone = "+221" + local9;

        Optional<User> optUser = userRepository
            .findOneWithAuthoritiesByLogin(phone)
            .or(() -> userRepository.findOneWithAuthoritiesByPhone(phone))
            .or(() -> userRepository.findOneWithAuthoritiesByPhone(intlPhone))
            .or(() -> userRepository.findOneWithAuthoritiesByPhone(local9))
            .or(() -> userRepository.findOneWithAuthoritiesByLogin(intlPhone))
            .or(() -> userRepository.findOneWithAuthoritiesByLogin(local9));

        if (optUser.isPresent()) {
            User existing = optUser.get();
            if (existing.getPhone() == null || existing.getPhone().isBlank()) {
                existing.setPhone(intlPhone);
                try {
                    return userRepository.save(existing);
                } catch (Exception e) {
                    LOG.warn("Impossible de mettre à jour le téléphone de l'utilisateur existant: {}", e.getMessage());
                }
            }
            return existing;
        }

        User newUser = new User();
        newUser.setLogin(intlPhone);
        newUser.setPhone(intlPhone);
        newUser.setPassword(passwordEncoder.encode(intlPhone + "_fotolou_secret_key"));
        newUser.setActivated(true);
        newUser.setLangKey("fr");

        if (optionalName != null && !optionalName.isBlank()) {
            String[] parts = optionalName.trim().split(" ", 2);
            newUser.setFirstName(parts[0]);
            if (parts.length > 1) {
                newUser.setLastName(parts[1]);
            }
        } else {
            newUser.setFirstName("Client");
            newUser.setLastName(local9.length() >= 4 ? local9.substring(local9.length() - 4) : local9);
        }

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.CLIENT).ifPresent(authorities::add);
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);

        return userRepository.save(newUser);
    }
}
