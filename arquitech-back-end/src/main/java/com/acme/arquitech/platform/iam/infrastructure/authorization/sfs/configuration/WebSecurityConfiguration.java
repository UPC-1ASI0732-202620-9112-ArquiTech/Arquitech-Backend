package com.acme.arquitech.platform.iam.infrastructure.authorization.sfs.configuration;

import com.acme.arquitech.platform.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import com.acme.arquitech.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BearerTokenService tokens,
            UserDetailsService users, AuthenticationEntryPoint entryPoint, ObjectMapper mapper,
            @Value("${cors.allowed-origins}") String allowedOrigins) throws Exception {
        var origins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (origins.stream().anyMatch(origin -> origin.contains("*")))
            throw new IllegalArgumentException("CORS origins must be explicit");
        http.cors(config -> config.configurationSource(request -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(origins);
            cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
            cors.setExposedHeaders(List.of("Location", "Content-Disposition"));
            return cors;
        }));
        http.csrf(config -> config.disable())
            .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(config -> config.authenticationEntryPoint(entryPoint)
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    mapper.writeValue(response.getOutputStream(), ErrorResponse.of("FORBIDDEN", "Access denied", request.getRequestURI()));
                }))
            .authorizeHttpRequests(config -> config
                .requestMatchers(HttpMethod.POST, "/api/v1/authentication/sign-in", "/api/v1/authentication/sign-up").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/projects/**", "/api/v1/materials/**",
                        "/api/v1/machinery/**", "/api/v1/workers/**", "/api/v1/tasks/**", "/api/v1/incidents/**").hasAuthority("SUPERVISOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/projects/**", "/api/v1/materials/**",
                        "/api/v1/machinery/**", "/api/v1/workers/**", "/api/v1/tasks/**", "/api/v1/incidents/**").hasAuthority("SUPERVISOR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/projects/**", "/api/v1/materials/**",
                        "/api/v1/machinery/**", "/api/v1/workers/**", "/api/v1/tasks/**", "/api/v1/incidents/**").hasAuthority("SUPERVISOR")
                .anyRequest().authenticated());
        // Construct here so Spring Boot does not also register this filter outside the security chain.
        http.addFilterBefore(new BearerAuthorizationRequestFilter(tokens, users), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
