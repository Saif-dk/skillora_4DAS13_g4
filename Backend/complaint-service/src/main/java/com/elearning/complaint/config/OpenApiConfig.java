package com.elearning.complaint.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI complaintServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Complaint Service API")
                        .version("1.0")
                        .description("""
                                Microservice de gestion des réclamations – plateforme Skillora (ESPRIT, Architecture Web Distribuée, 4DAS13).
                                
                                Ce service ne crée pas de tokens JWT. Pour obtenir un token, effectuez d'abord une requête POST sur user-service :
                                
                                POST http://localhost:8086/api/v1/auth/login
                                
                                Body JSON : { "email": "admin@elearning.com", "password": "Admin123!" }
                                
                                Copiez le token reçu, puis cliquez sur le bouton "Authorize" ci-dessous et collez-le SANS le préfixe "Bearer ".
                                """))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
