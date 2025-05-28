package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.Visit;
import com.example.medicalhomevisit.models.entities.VisitProtocol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VisitProtocolRepository extends JpaRepository<VisitProtocol, UUID> {
    Optional<VisitProtocol> findByVisit_Id(UUID visitId);
    Optional<VisitProtocol> findByVisit(Visit visit);
}
