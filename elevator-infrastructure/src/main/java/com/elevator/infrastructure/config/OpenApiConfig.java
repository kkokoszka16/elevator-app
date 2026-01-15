package com.elevator.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Elevator System API")
                        .description("REST API for elevator system simulation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Elevator System Team")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local development server")
                ));
    }
}
