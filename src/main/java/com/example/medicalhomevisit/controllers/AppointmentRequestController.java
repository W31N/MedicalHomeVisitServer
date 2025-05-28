package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.CreateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointment-requests")
public class AppointmentRequestController {

    @PostMapping
    public ResponseEntity<AppointmentRequestDto> createRequest(@RequestBody CreateRequestDto request) {
        // Создание заявки на визит
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentRequestDto>> getRequestsForPatient(@PathVariable UUID patientId) {
        // Заявки пациента
    }

    @PutMapping("/{requestId}/assign")
    public ResponseEntity<?> assignRequest(@PathVariable UUID requestId, @RequestBody AssignRequestDto request) {
        // Назначение врача на заявку
    }
}
