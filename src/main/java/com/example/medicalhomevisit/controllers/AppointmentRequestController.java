package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.service.AppointmentRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointment-requests")
public class AppointmentRequestController {

    @Autowired
    private AppointmentRequestService requestService;

    // Создание заявки (только для пациентов)
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentRequestDto> createRequest(@Valid @RequestBody CreateAppointmentRequestDto request) {
        AppointmentRequestDto created = requestService.createRequestByPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Получение своих заявок (для пациентов)
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentRequestDto>> getMyRequests() {
        List<AppointmentRequestDto> requests = requestService.getMyRequests();
        return ResponseEntity.ok(requests);
    }

    // Получение заявки по ID
    @GetMapping("/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentRequestDto> getRequestById(@PathVariable UUID requestId) {
        AppointmentRequestDto request = requestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }

    // Получение заявок конкретного пациента
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AppointmentRequestDto>> getRequestsForPatient(@PathVariable UUID patientId) {
        List<AppointmentRequestDto> requests = requestService.getRequestsForPatient(patientId);
        return ResponseEntity.ok(requests);
    }

    // Получение всех активных заявок (для админа/диспетчера)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<AppointmentRequestDto>> getAllActiveRequests() {
        List<AppointmentRequestDto> requests = requestService.getAllActiveRequests();
        return ResponseEntity.ok(requests);
    }

    // Назначение медработника на заявку
    @PutMapping("/{requestId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<AppointmentRequestDto> assignRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody AssignStaffToRequestDto request) {
        AppointmentRequestDto updated = requestService.assignStaffToRequest(requestId, request);
        return ResponseEntity.ok(updated);
    }

    // Обновление статуса заявки
    @PutMapping("/{requestId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppointmentRequestDto> updateRequestStatus(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateRequestStatusDto request) {
        AppointmentRequestDto updated = requestService.updateRequestStatus(requestId, request);
        return ResponseEntity.ok(updated);
    }

    // Отмена заявки пациентом
    @PutMapping("/{requestId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentRequestDto> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam String reason) {
        AppointmentRequestDto cancelled = requestService.cancelRequest(requestId, reason);
        return ResponseEntity.ok(cancelled);
    }
}