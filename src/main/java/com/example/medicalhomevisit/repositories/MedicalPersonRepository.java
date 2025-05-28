package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalPersonRepository extends JpaRepository<MedicalPerson, UUID> {
    Optional<MedicalPerson> findByUser(UserEntity user);

    Optional<MedicalPerson> findByUserId(UUID userId);

    List<MedicalPerson> findBySpecializationContainingIgnoreCase(String specialization);

    List<MedicalPerson> findByUser_Active(boolean active);
}
