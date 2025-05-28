package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.UserEntity;
import com.example.medicalhomevisit.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByRole_Name(UserRole roleName);

    List<UserEntity> findByRole_NameAndActive(UserRole roleName, boolean active);

    boolean existsByEmail(String email);
}
