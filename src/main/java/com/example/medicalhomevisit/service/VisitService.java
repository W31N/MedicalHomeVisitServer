package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.models.enums.VisitStatus;
import com.example.medicalhomevisit.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitService.class);

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicalPersonRepository medicalPersonRepository;
    @Autowired
    private PatientRepository patientRepository;

    /**
     * Получить все визиты для конкретного медработника
     */
    @Transactional(readOnly = true)
    public List<VisitDto> getVisitsForMedicalStaff(UUID staffId) {
        log.info("SERVICE: Getting visits for medical staff ID: {}", staffId);

        // Проверяем права доступа
        checkAccessToStaffVisits(staffId);

        MedicalPerson medicalPerson = medicalPersonRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Медработник не найден: " + staffId));

        List<Visit> visits = visitRepository.findByAppointmentRequest_MedicalPerson(medicalPerson);
        log.info("SERVICE: Found {} visits for medical staff {}", visits.size(), staffId);

        return visits.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить визиты для текущего авторизованного медработника
     */
    @Transactional(readOnly = true)
    public List<VisitDto> getMyVisits() {
        log.info("SERVICE: Getting visits for current medical staff");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + currentUserEmail));

        if (currentUser.getRole().getName() != UserRole.MEDICAL_STAFF) {
            throw new AccessDeniedException("Только медицинские работники могут получать свои визиты");
        }

        MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Профиль медработника не найден для пользователя: " + currentUserEmail));

        List<Visit> visits = visitRepository.findByAppointmentRequest_MedicalPerson(medicalPerson);
        log.info("SERVICE: Found {} visits for current medical staff", visits.size());

        return visits.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить визиты на конкретную дату для медработника
     */
    @Transactional(readOnly = true)
    public List<VisitDto> getVisitsForDate(LocalDate date, UUID staffId) {
        log.info("SERVICE: Getting visits for date {} and staff ID: {}", date, staffId);

        checkAccessToStaffVisits(staffId);

        MedicalPerson medicalPerson = medicalPersonRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Медработник не найден: " + staffId));

        // Преобразуем LocalDate в начало и конец дня
        Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.setTime(startOfDay);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date startOfNextDay = cal.getTime();

        List<Visit> allVisits = visitRepository.findByAppointmentRequest_MedicalPerson(medicalPerson);
        List<Visit> visitsForDate = allVisits.stream()
                .filter(visit -> {
                    Date scheduledTime = visit.getScheduledTime();
                    return scheduledTime != null &&
                            !scheduledTime.before(startOfDay) &&
                            scheduledTime.before(startOfNextDay);
                })
                .collect(Collectors.toList());

        log.info("SERVICE: Found {} visits for date {} and staff {}", visitsForDate.size(), date, staffId);

        return visitsForDate.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить визиты на сегодня для текущего медработника
     */
    @Transactional(readOnly = true)
    public List<VisitDto> getMyVisitsForToday() {
        log.info("SERVICE: Getting today's visits for current medical staff");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + currentUserEmail));

        if (currentUser.getRole().getName() != UserRole.MEDICAL_STAFF) {
            throw new AccessDeniedException("Только медицинские работники могут получать свои визиты");
        }

        MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Профиль медработника не найден"));

        return getVisitsForDate(LocalDate.now(), medicalPerson.getId());
    }

    /**
     * Получить визит по ID
     */
    @Transactional(readOnly = true)
    public VisitDto getVisitById(UUID visitId) {
        log.info("SERVICE: Getting visit by ID: {}", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        return convertToDto(visit);
    }

    /**
     * Обновить статус визита
     */
    public VisitDto updateVisitStatus(UUID visitId, VisitStatus newStatus) {
        log.info("SERVICE: Updating visit {} status to {}", visitId, newStatus);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        visit.setStatus(newStatus);

        // Автоматически устанавливаем время начала/окончания
        if (newStatus == VisitStatus.IN_PROGRESS && visit.getActualStartTime() == null) {
            visit.setActualStartTime(new Date());
        } else if (newStatus == VisitStatus.COMPLETED && visit.getActualEndTime() == null) {
            visit.setActualEndTime(new Date());
        }

        Visit updatedVisit = visitRepository.save(visit);
        log.info("SERVICE: Visit status updated successfully");

        return convertToDto(updatedVisit);
    }

    /**
     * Обновить заметки к визиту
     */
    public VisitDto updateVisitNotes(UUID visitId, String notes) {
        log.info("SERVICE: Updating visit {} notes", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        visit.setNotes(notes);
        Visit updatedVisit = visitRepository.save(visit);
        log.info("SERVICE: Visit notes updated successfully");

        return convertToDto(updatedVisit);
    }

    /**
     * Обновить запланированное время визита
     */
    public VisitDto updateScheduledTime(UUID visitId, Date scheduledTime) {
        log.info("SERVICE: Updating visit {} scheduled time to {}", visitId, scheduledTime);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        visit.setScheduledTime(scheduledTime);
        Visit updatedVisit = visitRepository.save(visit);
        log.info("SERVICE: Visit scheduled time updated successfully");

        return convertToDto(updatedVisit);
    }

    /**
     * Начать визит (установить статус IN_PROGRESS и время начала)
     */
    public VisitDto startVisit(UUID visitId) {
        log.info("SERVICE: Starting visit {}", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        if (visit.getStatus() != VisitStatus.PLANNED) {
            throw new IllegalStateException("Можно начать только запланированный визит");
        }

        visit.setStatus(VisitStatus.IN_PROGRESS);
        visit.setActualStartTime(new Date());

        Visit updatedVisit = visitRepository.save(visit);
        log.info("SERVICE: Visit started successfully");

        return convertToDto(updatedVisit);
    }

    /**
     * Завершить визит (установить статус COMPLETED и время окончания)
     */
    public VisitDto completeVisit(UUID visitId) {
        log.info("SERVICE: Completing visit {}", visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Визит не найден: " + visitId));

        checkAccessToVisit(visit);

        if (visit.getStatus() != VisitStatus.IN_PROGRESS) {
            throw new IllegalStateException("Можно завершить только визит в процессе");
        }

        visit.setStatus(VisitStatus.COMPLETED);
        visit.setActualEndTime(new Date());

        Visit updatedVisit = visitRepository.save(visit);
        log.info("SERVICE: Visit completed successfully");

        return convertToDto(updatedVisit);
    }

    /**
     * Проверка прав доступа к визитам конкретного медработника
     */
    private void checkAccessToStaffVisits(UUID staffId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Админ и диспетчер могут видеть визиты любого медработника
        if (role == UserRole.ADMIN || role == UserRole.DISPATCHER) {
            return;
        }

        // Медработник может видеть только свои визиты
        if (role == UserRole.MEDICAL_STAFF) {
            MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                    .orElseThrow(() -> new EntityNotFoundException("Профиль медработника не найден"));

            if (!medicalPerson.getId().equals(staffId)) {
                throw new AccessDeniedException("Вы можете просматривать только свои визиты");
            }
            return;
        }

        throw new AccessDeniedException("У вас нет прав для просмотра визитов");
    }

    /**
     * Проверка прав доступа к конкретному визиту
     */
    private void checkAccessToVisit(Visit visit) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Админ и диспетчер могут видеть любые визиты
        if (role == UserRole.ADMIN || role == UserRole.DISPATCHER) {
            return;
        }

        // Медработник может видеть только свои визиты
        if (role == UserRole.MEDICAL_STAFF) {
            MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                    .orElse(null);
            if (medicalPerson != null &&
                    visit.getAppointmentRequest().getMedicalPerson() != null &&
                    medicalPerson.getId().equals(visit.getAppointmentRequest().getMedicalPerson().getId())) {
                return;
            }
        }

        // Пациент может видеть свои визиты
        if (role == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUser(currentUser)
                    .orElse(null);
            if (patient != null &&
                    visit.getAppointmentRequest().getPatient().getId().equals(patient.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("У вас нет доступа к этому визиту");
    }

    /**
     * Конвертация Entity в DTO
     */
    private VisitDto convertToDto(Visit entity) {
        VisitDto dto = new VisitDto();

        dto.setId(entity.getId());
        dto.setScheduledTime(entity.getScheduledTime());
        dto.setActualStartTime(entity.getActualStartTime());
        dto.setActualEndTime(entity.getActualEndTime());
        dto.setStatus(entity.getStatus().name());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Данные из связанной заявки
        AppointmentRequest request = entity.getAppointmentRequest();
        if (request != null) {
            dto.setAddress(request.getAddress());
            dto.setReasonForVisit(request.getSymptoms()); // symptoms становится reasonForVisit

            // Данные пациента
            if (request.getPatient() != null) {
                dto.setPatientId(request.getPatient().getId());
                if (request.getPatient().getUser() != null) {
                    dto.setPatientName(request.getPatient().getUser().getFullName());
                }
            }

            // Данные медработника
            if (request.getMedicalPerson() != null) {
                dto.setAssignedStaffId(request.getMedicalPerson().getId());
                if (request.getMedicalPerson().getUser() != null) {
                    dto.setAssignedStaffName(request.getMedicalPerson().getUser().getFullName());
                }
            }
        }

        return dto;
    }
}