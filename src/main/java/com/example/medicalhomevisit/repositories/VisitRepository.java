package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.Patient;
import com.example.medicalhomevisit.models.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VisitRepository extends JpaRepository<Visit, UUID> {
    List<Visit> findByAppointmentRequest_Patient_Id(UUID patientId);
    List<Visit> findByAppointmentRequest_Patient(Patient patient);

    List<Visit> findByAppointmentRequest_MedicalPerson_Id(UUID medicalPersonId);
    List<Visit> findByAppointmentRequest_MedicalPerson(MedicalPerson medicalPerson);
}
