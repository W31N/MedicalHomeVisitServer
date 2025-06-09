package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.service.AppointmentRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointment-requests")
public class AppointmentRequestController {

    private AppointmentRequestService requestService;

    @PostMapping
    public ResponseEntity<AppointmentRequestDto> createRequest(@Valid @RequestBody CreateAppointmentRequestDto request) {
        AppointmentRequestDto created = requestService.createRequestByPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentRequestDto>> getMyRequests() {
        List<AppointmentRequestDto> requests = requestService.getMyRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<AppointmentRequestDto> getRequestById(@PathVariable UUID requestId) {
        AppointmentRequestDto request = requestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentRequestDto>> getRequestsForPatient(@PathVariable UUID patientId) {
        List<AppointmentRequestDto> requests = requestService.getRequestsForPatient(patientId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/active")
    public ResponseEntity<List<AppointmentRequestDto>> getAllActiveRequests() {
        List<AppointmentRequestDto> requests = requestService.getAllActiveRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{requestId}/assign")
    public ResponseEntity<AppointmentRequestDto> assignRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody AssignStaffToRequestDto request) {
        AppointmentRequestDto updated = requestService.assignStaffToRequest(requestId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<AppointmentRequestDto> updateRequestStatus(
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateRequestStatusDto request) {
        AppointmentRequestDto updated = requestService.updateRequestStatus(requestId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<AppointmentRequestDto> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam String reason) {
        AppointmentRequestDto cancelled = requestService.cancelRequest(requestId, reason);
        return ResponseEntity.ok(cancelled);
    }

    @Autowired
    public void setRequestService(AppointmentRequestService requestService) {
        this.requestService = requestService;
    }
}