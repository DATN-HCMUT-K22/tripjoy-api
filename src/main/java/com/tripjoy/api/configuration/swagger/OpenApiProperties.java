package com.tripjoy.api.configuration.swagger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!prod")
@ConfigurationProperties(prefix = "api.swagger")
@Getter
@Setter
public class OpenApiProperties {

    private Info info = new Info();
    private Servers servers = new Servers();

    @Getter
    @Setter
    public static class Info {
        private String title;
        private String version;
        private String description;
        private License license = new License();
        private Contact contact = new Contact();
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }

    @Getter
    @Setter
    public static class Contact {
        private String name;
        private String email;
        private String url;
    }

    @Getter
    @Setter
    public static class Servers {
        private ServerConfig dev = new ServerConfig();
        private ServerConfig prod = new ServerConfig();
    }

    @Getter
    @Setter
    public static class ServerConfig {
        private String url;
        private String description;
    }
}