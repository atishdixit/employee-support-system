package com.ext.emp.support.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the "bearerAuth" HTTP Bearer scheme referenced by @SecurityRequirement on the
 * protected controllers, so Swagger UI's "Authorize" button can carry a JWT on try-it-out
 * calls. Reachable at /swagger-ui/index.html once the app is running.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI employeeSupportOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Support System API")
                        .description("AI assistant for company policy questions, backed by a local Ollama model.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
