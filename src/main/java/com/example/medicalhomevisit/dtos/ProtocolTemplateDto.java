package com.example.medicalhomevisit.dtos;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ProtocolTemplateDto {
    private UUID id;
    private String name;
    private String description;
    private String complaintsTemplate;
    private String anamnesisTemplate;
    private String objectiveStatusTemplate;
    private String recommendationsTemplate;
    private List<String> requiredVitals;
    private Date createdAt;
    private Date updatedAt;

    public ProtocolTemplateDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComplaintsTemplate() {
        return complaintsTemplate;
    }

    public void setComplaintsTemplate(String complaintsTemplate) {
        this.complaintsTemplate = complaintsTemplate;
    }

    public String getAnamnesisTemplate() {
        return anamnesisTemplate;
    }

    public void setAnamnesisTemplate(String anamnesisTemplate) {
        this.anamnesisTemplate = anamnesisTemplate;
    }

    public String getObjectiveStatusTemplate() {
        return objectiveStatusTemplate;
    }

    public void setObjectiveStatusTemplate(String objectiveStatusTemplate) {
        this.objectiveStatusTemplate = objectiveStatusTemplate;
    }

    public String getRecommendationsTemplate() {
        return recommendationsTemplate;
    }

    public void setRecommendationsTemplate(String recommendationsTemplate) {
        this.recommendationsTemplate = recommendationsTemplate;
    }

    public List<String> getRequiredVitals() {
        return requiredVitals;
    }

    public void setRequiredVitals(List<String> requiredVitals) {
        this.requiredVitals = requiredVitals;
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
