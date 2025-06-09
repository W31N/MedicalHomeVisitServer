package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.MedicalPersonDto; // Наша новая DTO
import com.example.medicalhomevisit.models.entities.MedicalPerson;
import com.example.medicalhomevisit.models.entities.UserEntity;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.MedicalPersonRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalPersonService {
    private static final Logger log = LoggerFactory.getLogger(MedicalPersonService.class);

    private MedicalPersonRepository medicalPersonRepository;
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<MedicalPersonDto> getActiveMedicalStaff() {
        log.info("SERVICE: Fetching all active medical staff.");
        List<MedicalPerson> activeStaff = medicalPersonRepository.findActiveStaffByRole(UserRole.MEDICAL_STAFF);
        log.info("SERVICE: Found {} active medical staff members.", activeStaff.size());
        return activeStaff.stream()
                .map(this::convertToMedicalStaffDto)
                .collect(Collectors.toList());
    }

    private MedicalPersonDto convertToMedicalStaffDto(MedicalPerson medicalPersonEntity) {
        UserEntity userEntity = medicalPersonEntity.getUser();
        if (userEntity == null) {
            log.warn("MedicalPerson with ID {} has no associated UserEntity.", medicalPersonEntity.getId());
            return new MedicalPersonDto(medicalPersonEntity.getId(), null, "Имя не найдено", medicalPersonEntity.getSpecialization());
        }

        return new MedicalPersonDto(
                medicalPersonEntity.getId(),
                userEntity.getId(),
                userEntity.getFullName(),
                medicalPersonEntity.getSpecialization()
        );
    }

    @Autowired
    public void setMedicalPersonRepository(MedicalPersonRepository medicalPersonRepository) {
        this.medicalPersonRepository = medicalPersonRepository;
    }

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
}