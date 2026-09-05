package com.fotolou.app.web.rest;

import static com.fotolou.app.security.SecurityUtils.AUTHORITIES_CLAIM;
import static com.fotolou.app.security.SecurityUtils.JWT_ALGORITHM;
import static com.fotolou.app.security.SecurityUtils.TOKEN_TYPE_CLAIM;
import static com.fotolou.app.security.SecurityUtils.USER_ID_CLAIM;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fotolou.app.security.DomainUserDetailsService.UserWithId;
import com.fotolou.app.web.rest.vm.LoginVM;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to authenticate users.
 */
@Tag(name = "1. Authentification & OTP", description = "Authentification JWT administrateur et tokens de session")
@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateController.class);

    private final JwtEncoder jwtEncoder;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:900}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:3888000}")
    private long tokenValidityInSecondsForRememberMe;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthenticateController(JwtEncoder jwtEncoder, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());

        var authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = this.createToken(authentication);
        String refreshToken = this.createRefreshToken(authentication);
        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(jwt);
        return new ResponseEntity<>(new JWTToken(jwt, refreshToken), httpHeaders, HttpStatus.OK);
    }

    /**
     * {@code GET /authenticate} : check if the user is authenticated.
     *
     * @return the {@link ResponseEntity} with status {@code 204 (No Content)},
     * or with status {@code 401 (Unauthorized)} if not authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        var now = Instant.now();
        Instant validity = now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities)
            .claim(TOKEN_TYPE_CLAIM, "ACCESS");
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    public String createRefreshToken(Authentication authentication) {
        var now = Instant.now();
        Instant validity = now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS);

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(TOKEN_TYPE_CLAIM, "REFRESH");
        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;
        private String refreshToken;

        JWTToken(String idToken, String refreshToken) {
            this.idToken = idToken;
            this.refreshToken = refreshToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        @JsonProperty("token")
        String getToken() {
            return idToken;
        }

        @JsonProperty("refresh_token")
        String getRefreshToken() {
            return refreshToken;
        }

        @JsonProperty("refreshToken")
        String getAltRefreshToken() {
            return refreshToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }

        void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
