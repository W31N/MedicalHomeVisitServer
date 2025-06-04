package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.PatientDto;
import com.example.medicalhomevisit.dtos.PatientProfileUpdateDto;
import com.example.medicalhomevisit.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger log = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientService patientService;

    /**
     * Получить пациента по ID
     * GET /api/patients/{patientId}
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable UUID patientId) {
        log.info("API: GET /api/patients/{} - Getting patient by ID", patientId);
        PatientDto patient = patientService.getPatientById(patientId);
        log.info("API: Returning patient {}", patientId);
        return ResponseEntity.ok(patient);
    }

    /**
     * Поиск пациентов по имени
     * GET /api/patients/search?query=John
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(@RequestParam String query) {
        log.info("API: GET /api/patients/search - Searching patients with query: {}", query);
        List<PatientDto> patients = patientService.searchPatients(query);
        log.info("API: Returning {} patients for search query", patients.size());
        return ResponseEntity.ok(patients);
    }

    /**
     * Получить всех пациентов (только для админов)
     * GET /api/patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        log.info("API: GET /api/patients - Getting all patients");
        List<PatientDto> patients = patientService.getAllPatients();
        log.info("API: Returning {} patients total", patients.size());
        return ResponseEntity.ok(patients);
    }

    /**
     * Получить профиль текущего пациента
     * GET /api/patients/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<PatientDto> getMyProfile() {
        log.info("API: GET /api/patients/profile - Getting current patient profile");
        PatientDto patient = patientService.getMyProfile();
        log.info("API: Returning current patient profile");
        return ResponseEntity.ok(patient);
    }

    /**
     * Обновить профиль текущего пациента
     * PUT /api/patients/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<PatientDto> updateMyProfile(@Valid @RequestBody PatientProfileUpdateDto updateDto) {
        log.info("API: PUT /api/patients/profile - Updating current patient profile");
        PatientDto updatedPatient = patientService.updateMyProfile(updateDto);
        log.info("API: Patient profile updated successfully");
        return ResponseEntity.ok(updatedPatient);
    }
}