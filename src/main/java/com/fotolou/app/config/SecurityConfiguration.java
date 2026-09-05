package com.fotolou.app.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.fotolou.app.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;

    public SecurityConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/authenticate")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/salons", "/api/salons/**")
                    .permitAll()
                    .requestMatchers("/api/coiffeurs", "/api/coiffeurs/**", "/api/coiffeur-profiles", "/api/coiffeur-profiles/**")
                    .permitAll()
                    .requestMatchers("/api/products", "/api/products/**")
                    .permitAll()
                    .requestMatchers("/api/categories", "/api/categories/**")
                    .permitAll()
                    .requestMatchers("/api/product-categories", "/api/product-categories/**")
                    .permitAll()
                    .requestMatchers("/api/files/**", "/api/storage/**")
                    .permitAll()
                    .requestMatchers("/api/realtime/**")
                    .permitAll()
                    .requestMatchers("/api/platform-settings", "/api/platform-settings/**")
                    .permitAll()
                    .requestMatchers("/api/tickets", "/api/tickets/**")
                    .permitAll()
                    .requestMatchers("/api/orders", "/api/orders/**")
                    .permitAll()
                    .requestMatchers("/api/favorites", "/api/favorites/**")
                    .permitAll()
                    .requestMatchers("/api/relatives", "/api/relatives/**")
                    .permitAll()
                    .requestMatchers("/api/notifications", "/api/notifications/**")
                    .permitAll()
                    .requestMatchers("/api/app-notifications", "/api/app-notifications/**")
                    .permitAll()
                    .requestMatchers("/api/register")
                    .permitAll()
                    .requestMatchers("/api/activate")
                    .permitAll()
                    .requestMatchers("/api/account/reset-password/init")
                    .permitAll()
                    .requestMatchers("/api/account/reset-password/finish")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .permitAll()
                    .requestMatchers("/websocket/**")
                    .permitAll()
                    .requestMatchers("/management/health")
                    .permitAll()
                    .requestMatchers("/management/health/**")
                    .permitAll()
                    .requestMatchers("/management/info")
                    .permitAll()
                    .requestMatchers("/management/prometheus")
                    .permitAll()
                    .requestMatchers("/management/**")
                    .hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/**")
                    .authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
