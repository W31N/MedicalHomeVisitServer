package com.example.medicalhomevisit.dtos;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class VisitProtocolDto {
    private UUID id;
    private UUID visitId;
    private UUID templateId;
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
    private Map<String, String> additionalVitals;
    private Date createdAt;
    private Date updatedAt;

    public VisitProtocolDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getVisitId() {
        return visitId;
    }

    public void setVisitId(UUID visitId) {
        this.visitId = visitId;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public String getComplaints() {
        return complaints;
    }

    public void setComplaints(String complaints) {
        this.complaints = complaints;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getObjectiveStatus() {
        return objectiveStatus;
    }

    public void setObjectiveStatus(String objectiveStatus) {
        this.objectiveStatus = objectiveStatus;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public void setDiagnosisCode(String diagnosisCode) {
        this.diagnosisCode = diagnosisCode;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Integer getSystolicBP() {
        return systolicBP;
    }

    public void setSystolicBP(Integer systolicBP) {
        this.systolicBP = systolicBP;
    }

    public Integer getDiastolicBP() {
        return diastolicBP;
    }

    public void setDiastolicBP(Integer diastolicBP) {
        this.diastolicBP = diastolicBP;
    }

    public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }

    public Map<String, String> getAdditionalVitals() {
        return additionalVitals;
    }

    public void setAdditionalVitals(Map<String, String> additionalVitals) {
        this.additionalVitals = additionalVitals;
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
