package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.VisitProtocolDto;
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class VisitProtocolService {

    private static final Logger log = LoggerFactory.getLogger(VisitProtocolService.class);

    private VisitProtocolRepository visitProtocolRepository;
    private VisitRepository visitRepository;
    private ProtocolTemplateRepository protocolTemplateRepository;
    private UserRepository userRepository;
    private MedicalPersonRepository medicalPersonRepository;
    private PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public VisitProtocolDto getProtocolForVisit(UUID visitId) {
        log.info("SERVICE: Getting protocol for visit ID: {}", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        VisitProtocol protocol = visitProtocolRepository.findByVisit_Id(visitId)
                .orElse(null);

        if (protocol == null) {
            log.info("SERVICE: No protocol found for visit {}", visitId);
            return null;
        }

        log.info("SERVICE: Found protocol for visit {}", visitId);
        return convertToDto(protocol);
    }

    public VisitProtocolDto saveProtocol(VisitProtocolDto protocolDto) {
        log.info("SERVICE: Saving protocol for visit ID: {}", protocolDto.getVisitId());

        Visit visit = visitRepository.findById(protocolDto.getVisitId())
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + protocolDto.getVisitId()));

        checkAccessToVisit(visit);

        VisitProtocol existingProtocol = visitProtocolRepository.findByVisit_Id(protocolDto.getVisitId())
                .orElse(null);

        VisitProtocol protocol;
        boolean isUpdate = false;

        if (existingProtocol != null) {
            log.info("SERVICE: Updating existing protocol for visit {}", protocolDto.getVisitId());
            protocol = existingProtocol;
            isUpdate = true;
        } else {
            log.info("SERVICE: Creating new protocol for visit {}", protocolDto.getVisitId());
            protocol = new VisitProtocol();
            protocol.setVisit(visit);
        }

        updateProtocolFromDto(protocol, protocolDto);

        VisitProtocol savedProtocol = visitProtocolRepository.save(protocol);
        log.info("SERVICE: Protocol {} successfully for visit {}",
                isUpdate ? "updated" : "created", protocolDto.getVisitId());

        return convertToDto(savedProtocol);
    }

    public VisitProtocolDto applyTemplate(UUID visitId, UUID templateId) {
        log.info("SERVICE: Applying template {} to visit {}", templateId, visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        ProtocolTemplate template = protocolTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Шаблон не найден: " + templateId));

        VisitProtocol protocol = visitProtocolRepository.findByVisit_Id(visitId)
                .orElseGet(() -> {
                    VisitProtocol newProtocol = new VisitProtocol();
                    newProtocol.setVisit(visit);
                    return newProtocol;
                });

        Float currentTemp = protocol.getTemperature();
        Integer currentSystolic = protocol.getSystolicBP();
        Integer currentDiastolic = protocol.getDiastolicBP();
        Integer currentPulse = protocol.getPulse();
        var currentAdditionalVitals = protocol.getAdditionalVitals();

        protocol.setProtocolTemplate(template);
        protocol.setComplaints(template.getComplaintsTemplate());
        protocol.setAnamnesis(template.getAnamnesisTemplate());
        protocol.setObjectiveStatus(template.getObjectiveStatusTemplate());
        protocol.setRecommendations(template.getRecommendationsTemplate());

        protocol.setTemperature(currentTemp);
        protocol.setSystolicBP(currentSystolic);
        protocol.setDiastolicBP(currentDiastolic);
        protocol.setPulse(currentPulse);
        protocol.setAdditionalVitals(currentAdditionalVitals);

        VisitProtocol savedProtocol = visitProtocolRepository.save(protocol);
        log.info("SERVICE: Template applied successfully to visit {}", visitId);

        return convertToDto(savedProtocol);
    }

    public void deleteProtocol(UUID visitId) {
        log.info("SERVICE: Deleting protocol for visit ID: {}", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        VisitProtocol protocol = visitProtocolRepository.findByVisit_Id(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Протокол не найден для визита: " + visitId));

        visitProtocolRepository.delete(protocol);
        log.info("SERVICE: Protocol deleted successfully for visit {}", visitId);
    }

    private void checkAccessToVisit(Visit visit) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        if (role == UserRole.ADMIN) {
            return;
        }

        if (role == UserRole.MEDICAL_STAFF) {
            MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                    .orElse(null);
            if (medicalPerson != null &&
                    visit.getAppointmentRequest().getMedicalPerson() != null &&
                    medicalPerson.getId().equals(visit.getAppointmentRequest().getMedicalPerson().getId())) {
                return;
            }
        }

        if (role == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUser(currentUser)
                    .orElse(null);
            if (patient != null &&
                    visit.getAppointmentRequest().getPatient().getId().equals(patient.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("У вас нет доступа к протоколу этого визита");
    }

    private void updateProtocolFromDto(VisitProtocol protocol, VisitProtocolDto dto) {
        protocol.setComplaints(dto.getComplaints());
        protocol.setAnamnesis(dto.getAnamnesis());
        protocol.setObjectiveStatus(dto.getObjectiveStatus());
        protocol.setDiagnosis(dto.getDiagnosis());
        protocol.setDiagnosisCode(dto.getDiagnosisCode());
        protocol.setRecommendations(dto.getRecommendations());
        protocol.setTemperature(dto.getTemperature());
        protocol.setSystolicBP(dto.getSystolicBP());
        protocol.setDiastolicBP(dto.getDiastolicBP());
        protocol.setPulse(dto.getPulse());
        protocol.setAdditionalVitals(dto.getAdditionalVitals());

        if (dto.getTemplateId() != null) {
            ProtocolTemplate template = protocolTemplateRepository.findById(dto.getTemplateId())
                    .orElse(null);
            protocol.setProtocolTemplate(template);
        }
    }

    private VisitProtocolDto convertToDto(VisitProtocol entity) {
        VisitProtocolDto dto = new VisitProtocolDto();

        dto.setId(entity.getId());
        dto.setVisitId(entity.getVisit().getId());
        dto.setTemplateId(entity.getProtocolTemplate() != null ? entity.getProtocolTemplate().getId() : null);
        dto.setComplaints(entity.getComplaints());
        dto.setAnamnesis(entity.getAnamnesis());
        dto.setObjectiveStatus(entity.getObjectiveStatus());
        dto.setDiagnosis(entity.getDiagnosis());
        dto.setDiagnosisCode(entity.getDiagnosisCode());
        dto.setRecommendations(entity.getRecommendations());
        dto.setTemperature(entity.getTemperature());
        dto.setSystolicBP(entity.getSystolicBP());
        dto.setDiastolicBP(entity.getDiastolicBP());
        dto.setPulse(entity.getPulse());
        dto.setAdditionalVitals(entity.getAdditionalVitals());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    @Autowired
    public void setVisitProtocolRepository(VisitProtocolRepository visitProtocolRepository) {
        this.visitProtocolRepository = visitProtocolRepository;
    }

    @Autowired
    public void setVisitRepository(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @Autowired
    public void setProtocolTemplateRepository(ProtocolTemplateRepository protocolTemplateRepository) {
        this.protocolTemplateRepository = protocolTemplateRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setMedicalPersonRepository(MedicalPersonRepository medicalPersonRepository) {
        this.medicalPersonRepository = medicalPersonRepository;
    }

    @Autowired
    public void setPatientRepository(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }
}
