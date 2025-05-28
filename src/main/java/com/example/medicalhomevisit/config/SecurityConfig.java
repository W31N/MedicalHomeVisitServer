package com.example.medicalhomevisit.config;

import com.example.medicalhomevisit.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для REST API
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll() // Разрешаем доступ к эндпоинтам аутентификации
                        // .requestMatchers("/api/admin/**").hasRole("ADMIN") // Пример защиты админских эндпоинтов
                        // .requestMatchers("/api/visits/**").hasAnyRole("MEDICAL_STAFF", "DISPATCHER") // Пример
                        .anyRequest().authenticated() // Все остальные запросы требуют аутентификации
                )
                // Для REST API обычно используется STATELESS сессия (например, с JWT)
                // Для начала можно оставить стандартную сессионную аутентификацию,
                // или настроить STATELESS, если сразу планируете JWT.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Или STATELESS для JWT
                );
        // .httpBasic(Customizer.withDefaults()); // Можно использовать Basic Auth для простоты на старте
        // .formLogin(Customizer.withDefaults()); // Или стандартную форму входа Spring Security

        // Если будете использовать JWT, здесь будет настройка фильтра для JWT
        return http.build();
    }
}