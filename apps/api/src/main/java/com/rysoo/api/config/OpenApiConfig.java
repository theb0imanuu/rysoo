package com.rysoo.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rysoo CV Optimization Platform API")
                        .version("1.0.0")
                        .description("REST API for CV ingestion, ATS analysis, AI-assisted optimization, and multi-format exports.")
                        .contact(new Contact().name("Rysoo Engineering").url("https://github.com/theb0imanuu/rysoo"))
                )
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter Supabase Auth access token")
                        )
                );
    }
}
