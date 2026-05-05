package com.uruz.frostbite.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FROSTBITE")
                        .description("API 문서")
                        .version("v1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("X-XSRF-TOKEN",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-XSRF-TOKEN")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("X-XSRF-TOKEN"));
    }
}
