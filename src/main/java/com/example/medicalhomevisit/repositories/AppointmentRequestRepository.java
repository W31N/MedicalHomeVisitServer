package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.AppointmentRequest;
import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, UUID> {
    List<AppointmentRequest> findByPatient(Patient patient);
    List<AppointmentRequest> findByMedicalPerson(MedicalPerson medicalPerson);
}
