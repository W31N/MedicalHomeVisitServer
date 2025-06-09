package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.MedicalPersonDto;
import com.example.medicalhomevisit.service.MedicalPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical-person")
public class MedicalPersonController {

    private MedicalPersonService medicalPersonService;

    @GetMapping("/active")
    public ResponseEntity<List<MedicalPersonDto>> getActiveMedicalStaff() {
        List<MedicalPersonDto> medicalStaffList = medicalPersonService.getActiveMedicalStaff();
        return ResponseEntity.ok(medicalStaffList);
    }

    @Autowired
    public void setMedicalPersonService(MedicalPersonService medicalPersonService) {
        this.medicalPersonService = medicalPersonService;
    }
}
