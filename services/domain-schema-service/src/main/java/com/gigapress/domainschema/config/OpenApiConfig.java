package com.gigapress.domainschema.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Domain/Schema Service API")
                        .description("AI-powered domain model and schema generation service")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GigaPress Team")
                                .email("team@gigapress.ai")));
    }
}
