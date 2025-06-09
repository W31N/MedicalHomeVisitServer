package com.example.medicalhomevisit.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AssignStaffToRequestDto {
    @NotNull
    private UUID staffId;
    private String assignmentNote;

    public UUID getStaffId() { return staffId; }
    public void setStaffId(UUID staffId) { this.staffId = staffId; }
    public String getAssignmentNote() { return assignmentNote; }
    public void setAssignmentNote(String assignmentNote) { this.assignmentNote = assignmentNote; }
}
