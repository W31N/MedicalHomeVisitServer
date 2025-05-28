package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.Role;
import com.example.medicalhomevisit.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(UserRole name);
}
