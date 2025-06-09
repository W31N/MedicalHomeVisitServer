package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.LoginRequest;
import com.example.medicalhomevisit.dtos.LoginResponse;
import com.example.medicalhomevisit.dtos.PatientSelfRegisterRequest;
import com.example.medicalhomevisit.dtos.UserDto;
import com.example.medicalhomevisit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = authService.signIn(request);
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody PatientSelfRegisterRequest request) {
        try {
            LoginResponse loginResponse = authService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.signOut();
        return ResponseEntity.ok("Выход из системы выполнен успешно");
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
