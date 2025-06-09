package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.Patient;
import com.example.medicalhomevisit.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByUser(UserEntity user);

    List<Patient> findByUser_FullNameContainingIgnoreCase(String fullName);
}
