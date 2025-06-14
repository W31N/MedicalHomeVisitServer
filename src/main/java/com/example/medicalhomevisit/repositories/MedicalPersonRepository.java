package com.example.medicalhomevisit.repositories;

import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.UserEntity;
import com.example.medicalhomevisit.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalPersonRepository extends JpaRepository<MedicalPerson, UUID> {
    Optional<MedicalPerson> findByUser(UserEntity user);

    @Query("SELECT mp FROM MedicalPerson mp JOIN mp.user u JOIN u.role r WHERE u.active = true AND r.name = :roleName")
    List<MedicalPerson> findActiveStaffByRole(@Param("roleName") UserRole roleName);
}
