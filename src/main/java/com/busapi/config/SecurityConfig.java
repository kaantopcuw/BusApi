package com.busapi.config;

import com.busapi.core.security.JwtAuthenticationFilter;
import com.busapi.core.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ENDPOINTS ---
                        .requestMatchers(
                                "/api/xss-test/**",
                                "/api/v1/auth/**",          // Auth modülü
                                "/api/v1/public/**",        // Varsa public API'ler
                                "/api/v1/locations/**",     // Lokasyonları public yapmıştık
                                "/api/v1/sales/trip/*/seats", // Koltuk durumu public
                                "/api/v1/voyages/trips/search", // Sefer arama public
                                "/api/v1/voyages/search/route-map", // Harita public

                                // --- SWAGGER UI & OPENAPI ---
                                "/v3/api-docs/**",          // OpenAPI JSON verisi
                                "/swagger-ui/**",           // Swagger Arayüz dosyaları (CSS, JS, HTML)
                                "/swagger-ui.html"          // Swagger Ana sayfası
                        ).permitAll()

                        // --- DİĞER HER ŞEY İÇİN TOKEN ŞART ---
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}