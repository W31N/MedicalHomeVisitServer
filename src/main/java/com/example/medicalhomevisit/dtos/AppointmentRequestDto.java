package com.example.medicalhomevisit.dtos;

import com.example.medicalhomevisit.models.enums.RequestStatus;
import com.example.medicalhomevisit.models.enums.RequestType;
import java.util.Date;
import java.util.UUID;

public class AppointmentRequestDto {
    private UUID id;
    private String emailPatient;
    private String address;
    private RequestType requestType;
    private String symptoms;
    private String additionalNotes;
    private Date preferredDateTime;
    private RequestStatus status;
    private String assignedStaffEmail;
    private String assignedByUserEmail;    // ID пользователя (админа/диспетчера), кто назначил
    private Date assignedAt;
    private String assignmentNote;
    private String responseMessage;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}