package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.Patient;
import com.example.medicalhomevisit.models.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface VisitRepository extends JpaRepository<Visit, UUID> {
    List<Visit> findByAppointmentRequest_Patient_Id(UUID patientId);
    List<Visit> findByAppointmentRequest_Patient(Patient patient);

    List<Visit> findByAppointmentRequest_MedicalPerson_Id(UUID medicalPersonId);
    List<Visit> findByAppointmentRequest_MedicalPerson(MedicalPerson medicalPerson);

    @Query("SELECT v FROM Visit v WHERE v.appointmentRequest.medicalPerson.id = :staffId " +
            "AND v.scheduledTime >= :startDate AND v.scheduledTime < :endDate")
    List<Visit> findByStaffIdAndDateRange(@Param("staffId") UUID staffId,
                                          @Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate);

    @Query("SELECT v FROM Visit v WHERE v.scheduledTime >= :startDate AND v.scheduledTime < :endDate")
    List<Visit> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
