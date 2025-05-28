package com.example.medicalhomevisit.models.entities;


import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "protocol_templates")
public class ProtocolTemplate extends BaseEntity {

    private String name;
    private String description;
    private String complaintsTemplate;
    private String anamnesisTemplate;
    private String objectiveStatusTemplate;
    private String recommendationsTemplate;
    private List<String> requiredVitals = new ArrayList<>();

    private Set<VisitProtocol> visitProtocols = new HashSet<>();

    public ProtocolTemplate() {
    }

    @Column(nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getComplaintsTemplate() {
        return complaintsTemplate;
    }

    public void setComplaintsTemplate(String complaintsTemplate) {
        this.complaintsTemplate = complaintsTemplate;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getAnamnesisTemplate() {
        return anamnesisTemplate;
    }

    public void setAnamnesisTemplate(String anamnesisTemplate) {
        this.anamnesisTemplate = anamnesisTemplate;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getObjectiveStatusTemplate() {
        return objectiveStatusTemplate;
    }

    public void setObjectiveStatusTemplate(String objectiveStatusTemplate) {
        this.objectiveStatusTemplate = objectiveStatusTemplate;
    }

    @Column(columnDefinition = "TEXT", nullable = true)
    public String getRecommendationsTemplate() {
        return recommendationsTemplate;
    }

    public void setRecommendationsTemplate(String recommendationsTemplate) {
        this.recommendationsTemplate = recommendationsTemplate;
    }

    @JdbcTypeCode(SqlTypes.JSON) // Для хранения List<String> как JSONB в PostgreSQL
    @Column(columnDefinition = "jsonb", nullable = true)
    public List<String> getRequiredVitals() {
        return requiredVitals;
    }

    public void setRequiredVitals(List<String> requiredVitals) {
        this.requiredVitals = requiredVitals;
    }

    @OneToMany(mappedBy = "protocolTemplate", fetch = FetchType.LAZY)
    public Set<VisitProtocol> getVisitProtocols() {
        return visitProtocols;
    }

    public void setVisitProtocols(Set<VisitProtocol> visitProtocols) {
        this.visitProtocols = visitProtocols;
    }
}
