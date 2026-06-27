package org.example.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 Configuration (SpringDoc).
 */
@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .title("Task Spring Core Demo API")
                .description("""
                        ## Training Management REST API
                        
                        This API provides endpoints for managing training-related resources.
                        
                        ### Authentication
                        - All secured endpoints require a valid **JWT Bearer token**.
                        - Obtain a token via the `/auth/login` endpoint.
                        - Click the **Authorize** button and paste your token.
                        
                        ### Notes
                        - All timestamps are in **UTC**.
                        - Pagination is supported on list endpoints.
                        """)
                .version("1.0.0");
    }

    private List<Server> buildServers() {
        Server localServer = new Server()
                .url(String.format("http://localhost:%d", serverPort))
                .description("Local Development Server");

        return List.of(localServer);
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        ### How to Authenticate
                        1. Call `POST /auth/login` with your credentials.
                        2. Copy the `accessToken` from the response.
                        3. Click **Authorize** and paste the token *(without the `Bearer` prefix)*.
                        4. All secured endpoints will now include the token automatically.
                        """);
    }
}