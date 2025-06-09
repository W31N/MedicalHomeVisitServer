package com.example.medicalhomevisit.config;

import com.example.medicalhomevisit.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/secure/hello").authenticated()
                        .requestMatchers("/api/secure/patient-only").hasRole("PATIENT")
                        .requestMatchers("/api/secure/admin-only").hasRole("ADMIN")
                        .requestMatchers("/api/visits/**").hasAnyRole("MEDICAL_STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/medical-person/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/appointment-requests").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointment-requests/my").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointment-requests/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/appointment-requests/*/assign").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/appointment-requests/*/cancel").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/appointment-requests/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/appointment-requests/patient/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/appointment-requests/*/status").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}