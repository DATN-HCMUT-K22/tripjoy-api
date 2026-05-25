package com.tripjoy.api.configuration.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tripjoy.api.constant.Endpoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    private static final String[] PUBLIC_ENDPOINTS = {
        Endpoint.Auth.BASE + Endpoint.Auth.REGISTER,
        Endpoint.Auth.BASE + Endpoint.Auth.LOGIN,
        Endpoint.Auth.BASE + Endpoint.Auth.INTROSPECT,
        Endpoint.Auth.BASE + Endpoint.Auth.LOGOUT,
        Endpoint.Auth.BASE + Endpoint.Auth.REFRESH,
        // Location resolve — public: any user (even unauthenticated) can resolve a map pick
        Endpoint.Location.BASE + Endpoint.Location.RESOLVE
    };

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**"
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
        Endpoint.Post.BASE,
        Endpoint.Post.BASE + Endpoint.Post.ID,
        Endpoint.Post.BASE + Endpoint.Post.COMMENTS,
        // Location endpoints — public search and autocomplete (no auth needed)
        Endpoint.Location.BASE + Endpoint.Location.ADMINISTRATIVE,
        Endpoint.Location.BASE + Endpoint.Location.SEARCH,
        Endpoint.Location.BASE + Endpoint.Location.NEARBY,
        Endpoint.Location.BASE + Endpoint.Location.AUTOCOMPLETE,
        Endpoint.Location.BASE + Endpoint.Location.ID
    };

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SWAGGER_WHITELIST)
                .permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                .permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(customJwtDecoder).jwtAuthenticationConverter(jwtConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return httpSecurity.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return authenticationConverter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
