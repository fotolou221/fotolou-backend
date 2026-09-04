package com.fotolou.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.jhipster.config.JHipsterConstants;

@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_API_DOCS)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .info(
                new Info()
                    .title("Fotolou API REST")
                    .description(
                        "Documentation complète de l'API Backend Fotolou (Authentification OTP SMS, Salons, File d'attente Tickets, Boutique, Favoris et Administration)."
                    )
                    .version("1.0.0")
                    .contact(new Contact().name("Support Fotolou").email("contact@fotolou.sn").url("https://fotolou.sn"))
                    .license(new License().name("Propriétaire").url("https://fotolou.sn"))
            )
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(
                new Components().addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                )
            );
    }

    @Bean
    public GroupedOpenApi fotolouAllGroupedOpenAPI() {
        return GroupedOpenApi.builder()
            .group("1-fotolou-complete")
            .displayName("🚀 Toutes les API Fotolou (Complète)")
            .packagesToScan("com.fotolou.app.web.rest", "com.fotolou.app.web.rest.custom")
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    public GroupedOpenApi fotolouCustomGroupedOpenAPI() {
        return GroupedOpenApi.builder()
            .group("2-fotolou-metier")
            .displayName("⭐ API Métier (OTP, Tickets, Commandes, Salons, Admin)")
            .packagesToScan("com.fotolou.app.web.rest.custom")
            .pathsToMatch("/api/**")
            .build();
    }
}
