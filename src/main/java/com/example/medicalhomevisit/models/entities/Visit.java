package com.example.medicalhomevisit.models.entities;

import com.example.medicalhomevisit.models.enums.VisitStatus;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {
    private AppointmentRequest appointmentRequest;
    private VisitProtocol protocol;

    private Date scheduledTime;
    private Date actualStartTime;
    private Date actualEndTime;
    private VisitStatus status;
    private String notes;

    public Visit() {
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false) // Визит всегда связан с заявкой
    @JoinColumn(name = "appointment_request_id", nullable = false, unique = true)
    public AppointmentRequest getAppointmentRequest() {
        return appointmentRequest;
    }

    public void setAppointmentRequest(AppointmentRequest appointmentRequest) {
        this.appointmentRequest = appointmentRequest;
    }

    @OneToOne(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    public VisitProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(VisitProtocol protocol) {
        this.protocol = protocol;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
    public Date getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(Date actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
