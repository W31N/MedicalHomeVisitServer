package com.example.medicalhomevisit.dtos;

import java.util.Date;
import java.util.UUID;

@Data
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
}
