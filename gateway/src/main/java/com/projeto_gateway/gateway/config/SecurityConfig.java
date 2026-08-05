package com.projeto_gateway.gateway.config;

import com.projeto_gateway.gateway.security.CustomAccessDeniedHandler;
import com.projeto_gateway.gateway.security.CustomAuthenticationEntryPoint;
import com.projeto_gateway.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/public/**", "/actuator/health", "/auth/login").permitAll()
                        .requestMatchers("/api/pedidos/**").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .headers(headers -> headers
                                // Configuração do Content-Security-Policy
                                .contentSecurityPolicy(csp -> csp.policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:;"
                                ))
                                // HSTS
                                .httpStrictTransportSecurity(hsts -> hsts
                                        .includeSubDomains(true)
                                        .maxAgeInSeconds(31536000)
                                        .preload(true)
                                )
                                // X-Content-Type-Options: nosniff
                                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                                // X-Frame-Options: DENY
                                .frameOptions(frameOptions -> frameOptions.deny())
                        // Remover cabeçalho "X-XSS-Protection" (já obsoleto)
                        // .xssProtection(xss -> xss.disable()) // opcional
                );

        return http.build();
    }
}