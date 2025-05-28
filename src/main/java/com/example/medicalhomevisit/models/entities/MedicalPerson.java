package com.example.medicalhomevisit.models.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "medical_staff")
public class MedicalPerson extends BaseEntity {
    private UserEntity user;
    private String specialization;

    public MedicalPerson() {
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Column()
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
