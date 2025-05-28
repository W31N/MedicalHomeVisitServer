package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.ProtocolTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProtocolTemplateRepository extends JpaRepository<ProtocolTemplate, UUID> {
    Optional<ProtocolTemplate> findByName(String name);
}
