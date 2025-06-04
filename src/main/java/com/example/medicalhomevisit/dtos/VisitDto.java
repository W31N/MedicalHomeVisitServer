package com.example.medicalhomevisit.dtos;

import java.util.Date;
import java.util.UUID;

public class VisitDto {
    private UUID id;
    private UUID patientId;
    private String patientName;
    private Date scheduledTime;
    private String status;
    private String address;
    private String reasonForVisit;
    private String notes;
    private UUID assignedStaffId;
    private String assignedStaffName;
    private Date actualStartTime;
    private Date actualEndTime;
    private Date createdAt;
    private Date updatedAt;

    public VisitDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(UUID assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public Date getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(Date actualStartTime) { this.actualStartTime = actualStartTime; }

    public Date getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(Date actualEndTime) { this.actualEndTime = actualEndTime; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
