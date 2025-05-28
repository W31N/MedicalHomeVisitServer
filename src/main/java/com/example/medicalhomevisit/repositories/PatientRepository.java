package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.Patient;
import com.example.medicalhomevisit.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByUser_Email(String email);

    Optional<Patient> findByUser(UserEntity user);

    Optional<Patient> findByUserId(UUID userId);

    List<Patient> findByUser_FullNameContainingIgnoreCase(String fullNamePart);

    Optional<Patient> findByPhoneNumber(String phoneNumber);
}
