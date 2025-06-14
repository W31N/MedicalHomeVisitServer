package com.example.medicalhomevisit.dtos;

import java.util.UUID;

public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String role;
    private UUID medicalPersonId;

    public UserDto() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UUID getMedicalPersonId() {
        return medicalPersonId;
    }

    public void setMedicalPersonId(UUID medicalPersonId) {
        this.medicalPersonId = medicalPersonId;
    }
}