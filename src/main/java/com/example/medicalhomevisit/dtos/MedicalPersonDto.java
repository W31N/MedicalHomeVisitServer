package com.example.medicalhomevisit.dtos;

import java.util.UUID;

public class MedicalPersonDto {
    private UUID medicalPersonId;
    private UUID userId;
    private String fullName;
    private String specialization;

    public MedicalPersonDto(UUID medicalPersonId, UUID userId, String fullName, String specialization) {
        this.medicalPersonId = medicalPersonId;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
    }

    public UUID getMedicalPersonId() {
        return medicalPersonId;
    }

    public void setMedicalPersonId(UUID medicalPersonId) {
        this.medicalPersonId = medicalPersonId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}