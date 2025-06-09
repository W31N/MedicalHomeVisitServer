package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.VisitDto;
import com.example.medicalhomevisit.models.enums.VisitStatus;
import com.example.medicalhomevisit.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private static final Logger log = LoggerFactory.getLogger(VisitController.class);

    private VisitService visitService;

    @GetMapping("/my")
    public ResponseEntity<List<VisitDto>> getMyVisits() {
        log.info("API: GET /api/visits/my - Getting visits for current medical staff");
        List<VisitDto> visits = visitService.getMyVisits();
        log.info("API: Returning {} visits for current medical staff", visits.size());
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/my/today")
    public ResponseEntity<List<VisitDto>> getMyVisitsForToday() {
        log.info("API: GET /api/visits/my/today - Getting today's visits for current medical staff");
        List<VisitDto> visits = visitService.getMyVisitsForToday();
        log.info("API: Returning {} visits for today", visits.size());
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<VisitDto>> getVisitsForStaff(@PathVariable UUID staffId) {
        log.info("API: GET /api/visits/staff/{} - Getting visits for medical staff", staffId);
        List<VisitDto> visits = visitService.getVisitsForMedicalStaff(staffId);
        log.info("API: Returning {} visits for medical staff {}", visits.size(), staffId);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/staff/{staffId}/date/{date}")
    public ResponseEntity<List<VisitDto>> getVisitsForStaffAndDate(
            @PathVariable UUID staffId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("API: GET /api/visits/staff/{}/date/{} - Getting visits for staff and date", staffId, date);
        List<VisitDto> visits = visitService.getVisitsForDate(date, staffId);
        log.info("API: Returning {} visits for staff {} and date {}", visits.size(), staffId, date);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/my/date/{date}")
    public ResponseEntity<List<VisitDto>> getMyVisitsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("API: GET /api/visits/my/date/{} - Getting visits for current staff and date", date);

        List<VisitDto> myVisits = visitService.getMyVisits();
        if (myVisits.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        UUID staffId = myVisits.get(0).getAssignedStaffId();
        List<VisitDto> visits = visitService.getVisitsForDate(date, staffId);

        log.info("API: Returning {} visits for current staff and date {}", visits.size(), date);
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<VisitDto> getVisitById(@PathVariable UUID visitId) {
        log.info("API: GET /api/visits/{} - Getting visit by ID", visitId);
        VisitDto visit = visitService.getVisitById(visitId);
        log.info("API: Returning visit {}", visitId);
        return ResponseEntity.ok(visit);
    }

    @PutMapping("/{visitId}/status")
    public ResponseEntity<VisitDto> updateVisitStatus(
            @PathVariable UUID visitId,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("API: PUT /api/visits/{}/status - Updating visit status", visitId);

        String statusString = statusUpdate.get("status");
        if (statusString == null || statusString.trim().isEmpty()) {
            log.error("API: Status is required");
            return ResponseEntity.badRequest().build();
        }

        try {
            VisitStatus newStatus = VisitStatus.valueOf(statusString.trim().toUpperCase());
            VisitDto updatedVisit = visitService.updateVisitStatus(visitId, newStatus);
            log.info("API: Visit status updated successfully");
            return ResponseEntity.ok(updatedVisit);
        } catch (IllegalArgumentException e) {
            log.error("API: Invalid status value: {}", statusString);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{visitId}/notes")
    public ResponseEntity<VisitDto> updateVisitNotes(
            @PathVariable UUID visitId,
            @RequestBody Map<String, String> notesUpdate) {
        log.info("API: PUT /api/visits/{}/notes - Updating visit notes", visitId);

        String notes = notesUpdate.get("notes");
        if (notes == null) {
            notes = "";
        }

        VisitDto updatedVisit = visitService.updateVisitNotes(visitId, notes);
        log.info("API: Visit notes updated successfully");
        return ResponseEntity.ok(updatedVisit);
    }

    @PutMapping("/{visitId}/scheduled-time")
    public ResponseEntity<VisitDto> updateScheduledTime(
            @PathVariable UUID visitId,
            @RequestBody Map<String, Date> timeUpdate) {
        log.info("API: PUT /api/visits/{}/scheduled-time - Updating scheduled time", visitId);

        Date scheduledTime = timeUpdate.get("scheduledTime");
        if (scheduledTime == null) {
            log.error("API: Scheduled time is required");
            return ResponseEntity.badRequest().build();
        }

        VisitDto updatedVisit = visitService.updateScheduledTime(visitId, scheduledTime);
        log.info("API: Visit scheduled time updated successfully");
        return ResponseEntity.ok(updatedVisit);
    }

    @PostMapping("/{visitId}/start")
    public ResponseEntity<VisitDto> startVisit(@PathVariable UUID visitId) {
        log.info("API: POST /api/visits/{}/start - Starting visit", visitId);
        VisitDto updatedVisit = visitService.startVisit(visitId);
        log.info("API: Visit started successfully");
        return ResponseEntity.ok(updatedVisit);
    }

    @PostMapping("/{visitId}/complete")
    public ResponseEntity<VisitDto> completeVisit(@PathVariable UUID visitId) {
        log.info("API: POST /api/visits/{}/complete - Completing visit", visitId);
        VisitDto updatedVisit = visitService.completeVisit(visitId);
        log.info("API: Visit completed successfully");
        return ResponseEntity.ok(updatedVisit);
    }

    @Autowired
    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }
}