package com.example.medicalhomevisit.dtos;

import com.example.medicalhomevisit.models.enums.RequestStatus;
import com.example.medicalhomevisit.models.enums.RequestType;
import java.util.Date;
import java.util.UUID;

public class AppointmentRequestDto {
    private UUID id;
    private UUID patientId; // Добавить
    private String patientName; // Добавить
    private String patientPhone; // Добавить
    private String emailPatient;
    private String address;
    private RequestType requestType;
    private String symptoms;
    private String additionalNotes;
    private Date preferredDateTime;
    private RequestStatus status;
    private UUID assignedStaffId; // Добавить
    private String assignedStaffName; // Добавить
    private String assignedStaffEmail;
    private String assignedByUserEmail;
    private Date assignedAt;
    private String assignmentNote;
    private String responseMessage;
    private Date createdAt; // Добавить
    private Date updatedAt; // Добавить

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getEmailPatient() {
        return emailPatient;
    }

    public void setEmailPatient(String emailPatient) {
        this.emailPatient = emailPatient;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public Date getPreferredDateTime() {
        return preferredDateTime;
    }

    public void setPreferredDateTime(Date preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public UUID getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(UUID assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
    }

    public String getAssignedStaffEmail() {
        return assignedStaffEmail;
    }

    public void setAssignedStaffEmail(String assignedStaffEmail) {
        this.assignedStaffEmail = assignedStaffEmail;
    }

    public String getAssignedByUserEmail() {
        return assignedByUserEmail;
    }

    public void setAssignedByUserEmail(String assignedByUserEmail) {
        this.assignedByUserEmail = assignedByUserEmail;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignmentNote() {
        return assignmentNote;
    }

    public void setAssignmentNote(String assignmentNote) {
        this.assignmentNote = assignmentNote;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}