package com.example.medicalhomevisit.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// В каком-нибудь контроллере, например, TestController
@RestController
@RequestMapping("/api/secure")
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<String> getSecureHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Привет, аутентифицированный пользователь: " + authentication.getName());
    }

    @GetMapping("/patient-only")
    @PreAuthorize("hasRole('PATIENT')") // или hasAuthority('ROLE_PATIENT')
    public ResponseEntity<String> getPatientData() {
        return ResponseEntity.ok("Это данные только для пациента!");
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminData() {
        return ResponseEntity.ok("Это данные только для администратора!");
    }
}
