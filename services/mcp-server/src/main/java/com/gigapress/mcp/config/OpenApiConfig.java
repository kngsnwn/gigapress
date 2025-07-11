package com.gigapress.mcp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.port}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MCP Server API")
                .version("1.0.0")
                .description("Model Context Protocol Server - Core Tool APIs for GigaPress")
                .contact(new Contact()
                    .name("GigaPress Team")
                    .email("support@gigapress.com")
                    .url("https://gigapress.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.gigapress.com")
                    .description("Production Server")
            ));
    }
}
