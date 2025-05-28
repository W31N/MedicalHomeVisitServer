package com.example.medicalhomevisit.models.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "visit_protocols")
public class VisitProtocol extends BaseEntity {

    private Visit visit;
    private ProtocolTemplate protocolTemplate;

    private String complaints;
    private String anamnesis;
    private String objectiveStatus;
    private String diagnosis;
    private String diagnosisCode;
    private String recommendations;
    private Float temperature;
    private Integer systolicBP;
    private Integer diastolicBP;
    private Integer pulse;
    private Map<String, String> additionalVitals = new HashMap<>();

    public VisitProtocol() {
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "template_id", nullable = true)
    public ProtocolTemplate getProtocolTemplate() {
        return protocolTemplate;
    }

    public void setProtocolTemplate(ProtocolTemplate protocolTemplate) {
        this.protocolTemplate = protocolTemplate;
    }

    @Column(columnDefinition = "TEXT")
    public String getComplaints() {
        return complaints;
    }

    public void setComplaints(String complaints) {
        this.complaints = complaints;
    }

    @Column(columnDefinition = "TEXT")
    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    @Column(columnDefinition = "TEXT")
    public String getObjectiveStatus() {
        return objectiveStatus;
    }

    public void setObjectiveStatus(String objectiveStatus) {
        this.objectiveStatus = objectiveStatus;
    }

    @Column(columnDefinition = "TEXT")
    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    @Column(nullable = true)
    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    @Column(columnDefinition = "TEXT")
    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    @Column(name = "temperature")
    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Column(name = "systolic_bp")
    public Integer getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(Integer systolicBP) {
        this.systolicBP = systolicBP;
    }

    @Column(name = "diastolic_bp")
    public Integer getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(Integer diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    @Column(name = "pulse")
    public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = true)
    public Map<String, String> getAdditionalVitals() {
        return additionalVitals;
    }

    public void setAdditionalVitals(Map<String, String> additionalVitals) {
        this.additionalVitals = additionalVitals;
    }
}
