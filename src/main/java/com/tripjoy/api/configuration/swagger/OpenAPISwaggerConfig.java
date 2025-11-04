package com.tripjoy.api.configuration.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Profile("!prod")
@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
@RequiredArgsConstructor
public class OpenAPISwaggerConfig {
    private final OpenApiProperties properties;

    @Bean
    public OpenAPI customOpenAPI() {
        // Get value from properties
        OpenApiProperties.Servers serversProps = properties.getServers();
        OpenApiProperties.Info infoProps = properties.getInfo();
        OpenApiProperties.Contact contactProps = infoProps.getContact();
        OpenApiProperties.License licenseProps = infoProps.getLicense();

        // 1. Config Servers
        Server devServer = new Server();
        devServer.setUrl(serversProps.getDev().getUrl());
        devServer.setDescription(serversProps.getDev().getDescription());

        Server prodServer = new Server();
        prodServer.setUrl(serversProps.getProd().getUrl());
        prodServer.setDescription(serversProps.getProd().getDescription());

        // 2. Config Contact
        Contact contact = new Contact();
        contact.setEmail(contactProps.getEmail());
        contact.setName(contactProps.getName());
        contact.setUrl(contactProps.getUrl());

        // 3. Config License
        License mitLicense = new License()
                .name(licenseProps.getName())
                .url(licenseProps.getUrl());

        // 4. Config Info
        Info info = new Info()
                .title(infoProps.getTitle())
                .version(infoProps.getVersion())
                .contact(contact)
                .description(infoProps.getDescription())
                .license(mitLicense);

        // 5. Config Security
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // Loại là HTTP
                .scheme("bearer")               // Scheme là "bearer"
                .bearerFormat("JWT");           // Định dạng là "JWT"

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth"); // Tên phải khớp với key ở dưới

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .addSecurityItem(securityRequirement) // Áp dụng (Ổ khóa)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme)); // Định nghĩa (Nút Authorize)
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("tripjoy-api-service")
                .packagesToScan("com.tripjoy.api.controller")
                .build();
    }
}
