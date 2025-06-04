package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.PatientDto;
import com.example.medicalhomevisit.dtos.PatientProfileUpdateDto;
import com.example.medicalhomevisit.models.entities.Patient;
import com.example.medicalhomevisit.models.entities.UserEntity;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.PatientRepository;
import com.example.medicalhomevisit.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Получить пациента по ID
     */
    @Transactional(readOnly = true)
    public PatientDto getPatientById(UUID patientId) {
        log.info("SERVICE: Getting patient by ID: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Пациент не найден: " + patientId));

        checkAccessToPatient(patient);

        return convertToDto(patient);
    }

    /**
     * Поиск пациентов по имени (для медработников и админов)
     */
    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String query) {
        log.info("SERVICE: Searching patients with query: {}", query);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Только медработники, админы и диспетчеры могут искать пациентов
        if (role != UserRole.MEDICAL_STAFF && role != UserRole.ADMIN && role != UserRole.DISPATCHER) {
            throw new AccessDeniedException("У вас нет прав для поиска пациентов");
        }

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String searchQuery = "%" + query.toLowerCase().trim() + "%";
        List<Patient> patients = patientRepository.findByUser_FullNameContainingIgnoreCase(query.trim());

        log.info("SERVICE: Found {} patients for query: {}", patients.size(), query);

        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить всех пациентов (только для админов)
     */
    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        log.info("SERVICE: Getting all patients");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        if (role != UserRole.ADMIN) {
            throw new AccessDeniedException("Только администраторы могут просматривать всех пациентов");
        }

        List<Patient> patients = patientRepository.findAll();
        log.info("SERVICE: Found {} patients total", patients.size());

        return patients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Проверка прав доступа к пациенту
     */
    private void checkAccessToPatient(Patient patient) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Админ и диспетчер могут видеть любых пациентов
        if (role == UserRole.ADMIN || role == UserRole.DISPATCHER) {
            return;
        }

        // Медработники могут видеть пациентов (в контексте визитов)
        if (role == UserRole.MEDICAL_STAFF) {
            return;
        }

        // Пациент может видеть только свои данные
        if (role == UserRole.PATIENT) {
            Patient currentPatient = patientRepository.findByUser(currentUser)
                    .orElse(null);
            if (currentPatient != null && currentPatient.getId().equals(patient.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("У вас нет доступа к данным этого пациента");
    }

    /**
     * Конвертация Entity в DTO
     */
    private PatientDto convertToDto(Patient entity) {
        PatientDto dto = new PatientDto();

        dto.setId(entity.getId());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender() != null ? entity.getGender().name() : null);
        dto.setAddress(entity.getAddress());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setPolicyNumber(entity.getPolicyNumber());
        dto.setAllergies(entity.getAllergies());
        dto.setChronicConditions(entity.getChronicConditions());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Данные из связанного пользователя
        if (entity.getUser() != null) {
            dto.setFullName(entity.getUser().getFullName());
        }

        // Вычисляем возраст
        if (entity.getDateOfBirth() != null) {
            dto.setAge(calculateAge(entity.getDateOfBirth()));
        }

        return dto;
    }

    /**
     * Вычисление возраста
     */
    private Integer calculateAge(java.util.Date dateOfBirth) {
        if (dateOfBirth == null) return null;

        Calendar birthCalendar = Calendar.getInstance();
        birthCalendar.setTime(dateOfBirth);

        Calendar currentCalendar = Calendar.getInstance();

        int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return Math.max(age, 0);
    }

    @Transactional(readOnly = true)
    public PatientDto getMyProfile() {
        log.info("SERVICE: Getting current patient profile");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Проверяем, что это пациент
        if (currentUser.getRole().getName() != UserRole.PATIENT) {
            throw new AccessDeniedException("Только пациенты могут получать свой профиль");
        }

        // Находим запись пациента
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

        log.info("SERVICE: Found patient profile for user: {}", currentUserEmail);
        return convertToDto(patient);
    }

    @Transactional
    public PatientDto updateMyProfile(PatientProfileUpdateDto updateDto) {
        log.info("SERVICE: Updating current patient profile");

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        // Проверяем, что это пациент
        if (currentUser.getRole().getName() != UserRole.PATIENT) {
            throw new AccessDeniedException("Только пациенты могут обновлять свой профиль");
        }

        // Находим запись пациента
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

        // Обновляем поля (только если они переданы и не null)
        if (updateDto.getDateOfBirth() != null) {
            patient.setDateOfBirth(updateDto.getDateOfBirth());
        }

        if (updateDto.getGender() != null) {
            patient.setGender(updateDto.getGender());
        }

        if (updateDto.getAddress() != null) {
            patient.setAddress(updateDto.getAddress().trim());
        }

        if (updateDto.getPhoneNumber() != null) {
            patient.setPhoneNumber(updateDto.getPhoneNumber().trim());
        }

        if (updateDto.getPolicyNumber() != null) {
            patient.setPolicyNumber(updateDto.getPolicyNumber().trim());
        }

        if (updateDto.getAllergies() != null) {
            // Фильтруем пустые строки и удаляем лишние пробелы
            List<String> cleanAllergies = updateDto.getAllergies().stream()
                    .filter(allergy -> allergy != null && !allergy.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            patient.setAllergies(cleanAllergies);
        }

        if (updateDto.getChronicConditions() != null) {
            // Фильтруем пустые строки и удаляем лишние пробелы
            List<String> cleanConditions = updateDto.getChronicConditions().stream()
                    .filter(condition -> condition != null && !condition.trim().isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
            patient.setChronicConditions(cleanConditions);
        }

        // Обновляем timestamp
        patient.setUpdatedAt(new Date());

        // Сохраняем изменения
        Patient savedPatient = patientRepository.save(patient);

        log.info("SERVICE: Patient profile updated successfully for user: {}", currentUserEmail);
        return convertToDto(savedPatient);
    }
}