package com.example.demo.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.example.demo.jwt.JwtAuthEntryPoint;
import com.example.demo.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowCredentials(true);
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setMaxAge(3600L);
            return config;
        }));

        http.authorizeHttpRequests(authorize -> authorize
                // Статика и главная страница
                .requestMatchers(
                        "/",              // корень
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/static/**"
                ).permitAll()

                // Swagger
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll()

                // Публичные API эндпоинты авторизации/алертов
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/alerts",
                        "/api/alerts/**"
                ).permitAll()

                // Остальное требует аутентификации
                .anyRequest().authenticated()
        );

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthEntryPoint));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
