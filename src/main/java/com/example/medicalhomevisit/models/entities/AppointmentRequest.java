package com.example.medicalhomevisit.models.entities;

import com.example.medicalhomevisit.models.enums.RequestStatus;
import com.example.medicalhomevisit.models.enums.RequestType;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "appointment_requests")
public class AppointmentRequest extends BaseEntity {
    private Patient patient;
    private MedicalPerson medicalPerson;
    private UserEntity assignedBy;
    private String address;

    private RequestType requestType;
    private String symptoms;
    private String additionalNotes;
    private Date preferredDateTime;
    private RequestStatus status;

    private Date assignedAt;
    private String assignmentNote;
    private String responseMessage;

    public AppointmentRequest() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_person_id", nullable = true)
    public MedicalPerson getMedicalPerson() {
        return medicalPerson;
    }

    public void setMedicalPerson(MedicalPerson medicalPerson) {
        this.medicalPerson = medicalPerson;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = true)
    public UserEntity getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(UserEntity assignedBy) {
        this.assignedBy = assignedBy;
    }

    @Enumerated(EnumType.STRING) // Рекомендуется хранить enum как строку
    @Column(nullable = false)
    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    @Column(nullable = false, columnDefinition = "TEXT")
    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    @Column(name = "additional_notes", columnDefinition = "TEXT", nullable = true)
    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    public Date getPreferredDateTime() {
        return preferredDateTime;
    }

    public void setPreferredDateTime(Date preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "assigned_at", nullable = true)
    public Date getAssignedAt() {
        return assignedAt;
    }


    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
