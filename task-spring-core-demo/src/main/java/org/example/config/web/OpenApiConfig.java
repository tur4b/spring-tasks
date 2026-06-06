package org.example.config.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 Configuration (SpringDoc).
*/
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Spring Core Demo API")
                        .description("Training management REST API")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                                .name(BASIC_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("HTTP Basic Authentication")));
    }
}


