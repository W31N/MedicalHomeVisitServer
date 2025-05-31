package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.RequestStatus;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.models.enums.VisitStatus; // Для создания визита
import com.example.medicalhomevisit.repositories.*;
// import com.example.medicalhomevisit.exception.*; // Ваши кастомные исключения
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentRequestService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AppointmentRequestRepository requestRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MedicalPersonRepository medicalPersonRepository;
    @Autowired
    private VisitRepository visitRepository;

    public AppointmentRequestDto createRequestByPatient(CreateAppointmentRequestDto createDto) {
        String currentUserEmail  = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + currentUserEmail));

        if (userEntity.getRole().getName() != UserRole.PATIENT) {
            throw new AccessDeniedException("Только пациенты могут создавать заявки");
        }

        Patient patient = patientRepository.findByUser(userEntity)
                .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

        AppointmentRequest request = new AppointmentRequest();

        request.setPatient(patient);
        request.setRequestType(createDto.getRequestType());
        request.setSymptoms(createDto.getSymptoms());
        request.setAdditionalNotes(createDto.getAdditionalNotes());
        request.setStatus(RequestStatus.NEW);
        request.setPreferredDateTime(createDto.getPreferredDateTime());
        request.setAssignmentNote("");
        request.setResponseMessage("");

        AppointmentRequest savedRequest = requestRepository.save(request);
        return convertToDto(savedRequest);
    }


    @Transactional(readOnly = true)
    public AppointmentRequestDto getRequestById(UUID requestId) {
        AppointmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));

        checkAccessToRequest(request);

        return convertToDto(request);
    }


    // Получение заявок пациента
    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getRequestsForPatient(UUID patientId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Админ и медработники могут видеть заявки любого пациента
        if (role == UserRole.ADMIN || role == UserRole.MEDICAL_STAFF || role == UserRole.DISPATCHER) {
            return requestRepository.findByPatient_Id(patientId)
                    .stream().map(this::convertToDto).collect(Collectors.toList());
        }

        // Пациент может видеть только свои заявки
        if (role == UserRole.PATIENT) {
            Patient currentPatient = patientRepository.findByUser(currentUser)
                    .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

            if (!currentPatient.getId().equals(patientId)) {
                throw new AccessDeniedException("Вы можете просматривать только свои заявки");
            }

            return requestRepository.findByPatient_Id(patientId)
                    .stream().map(this::convertToDto).collect(Collectors.toList());
        }

        throw new AccessDeniedException("Недостаточно прав для просмотра заявок");
    }


    // Получение заявок текущего пациента
    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getMyRequests() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (currentUser.getRole().getName() != UserRole.PATIENT) {
            throw new AccessDeniedException("Только пациенты могут получать свои заявки");
        }

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

        return requestRepository.findByPatient_Id(patient.getId())
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }


    // Получение всех активных заявок (для админа/диспетчера)
    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getAllActiveRequests() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();
        if (role != UserRole.ADMIN && role != UserRole.DISPATCHER) {
            throw new AccessDeniedException("Только администратор или диспетчер могут просматривать все заявки");
        }

        List<RequestStatus> activeStatuses = List.of(
                RequestStatus.NEW, RequestStatus.PENDING,
                RequestStatus.ASSIGNED, RequestStatus.SCHEDULED
        );

        return requestRepository.findByStatusIn(activeStatuses)
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }


    // Назначение медработника на заявку
    public AppointmentRequestDto assignStaffToRequest(UUID requestId, AssignStaffToRequestDto assignDto) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();
        if (role != UserRole.ADMIN && role != UserRole.DISPATCHER) {
            throw new AccessDeniedException("Только администратор или диспетчер могут назначать медработников");
        }

        AppointmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));

        MedicalPerson staff = medicalPersonRepository.findById(assignDto.getStaffId())
                .orElseThrow(() -> new EntityNotFoundException("Медработник не найден: " + assignDto.getStaffId()));

        request.setMedicalPerson(staff);
        request.setAssignedBy(currentUser);
        request.setAssignedAt(new Date());
        request.setAssignmentNote(assignDto.getAssignmentNote());
        request.setStatus(RequestStatus.ASSIGNED);

        AppointmentRequest updatedRequest = requestRepository.save(request);

        // Автоматически создаем визит
        createVisitFromRequest(updatedRequest);

        // Обновляем статус на SCHEDULED
        request.setStatus(RequestStatus.SCHEDULED);
        updatedRequest = requestRepository.save(request);

        return convertToDto(updatedRequest);
    }


    // Обновление статуса заявки
    public AppointmentRequestDto updateRequestStatus(UUID requestId, UpdateRequestStatusDto statusDto) {
        AppointmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));

        // Проверка прав на изменение статуса
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        // Пациент может только отменить свою заявку
        if (role == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUser(currentUser)
                    .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден"));

            if (!request.getPatient().getId().equals(patient.getId())) {
                throw new AccessDeniedException("Вы можете изменять только свои заявки");
            }

            if (statusDto.getStatus() != RequestStatus.CANCELLED) {
                throw new AccessDeniedException("Пациент может только отменить заявку");
            }

            if (request.getStatus() != RequestStatus.NEW && request.getStatus() != RequestStatus.PENDING) {
                throw new IllegalStateException("Можно отменить только новые или ожидающие заявки");
            }
        }

        request.setStatus(statusDto.getStatus());
        if (statusDto.getResponseMessage() != null) {
            request.setResponseMessage(statusDto.getResponseMessage());
        }

        AppointmentRequest updatedRequest = requestRepository.save(request);
        return convertToDto(updatedRequest);
    }


    // Отмена заявки пациентом
    public AppointmentRequestDto cancelRequest(UUID requestId, String reason) {
        UpdateRequestStatusDto statusDto = new UpdateRequestStatusDto();
        statusDto.setStatus(RequestStatus.CANCELLED);
        statusDto.setResponseMessage(reason);
        return updateRequestStatus(requestId, statusDto);
    }

    private void createVisitFromRequest(AppointmentRequest request) {
        Visit visit = new Visit();
        visit.setAppointmentRequest(request);
        visit.setScheduledTime(request.getPreferredDateTime() != null ?
                request.getPreferredDateTime() : new Date());
        visit.setStatus(VisitStatus.PLANNED);
        visit.setNotes("Создан из заявки: " + request.getSymptoms());

        visitRepository.save(visit);
    }

    private void checkAccessToRequest(AppointmentRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        if (role == UserRole.ADMIN || role == UserRole.DISPATCHER) {
            return;
        }

        if (role == UserRole.MEDICAL_STAFF) {
            MedicalPerson medicalPerson = medicalPersonRepository.findByUser(currentUser)
                    .orElse(null);
            if (medicalPerson != null && request.getMedicalPerson() != null &&
                    medicalPerson.getId().equals(request.getMedicalPerson().getId())) {
                return;
            }
        }

        if (role == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUser(currentUser)
                    .orElse(null);
            if (patient != null && request.getPatient().getId().equals(patient.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("У вас нет доступа к этой заявке");
    }

    private AppointmentRequestDto convertToDto(AppointmentRequest entity) {
        AppointmentRequestDto dto = new AppointmentRequestDto();
        dto.setId(entity.getId());

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getUser().getFullName());
            dto.setPatientPhone(entity.getPatient().getPhoneNumber());
            dto.setEmailPatient(entity.getPatient().getUser().getEmail());
            dto.setAddress(entity.getPatient().getAddress());
        }

        dto.setRequestType(entity.getRequestType());
        dto.setSymptoms(entity.getSymptoms());
        dto.setAdditionalNotes(entity.getAdditionalNotes());
        dto.setPreferredDateTime(entity.getPreferredDateTime());
        dto.setStatus(entity.getStatus());

        if (entity.getMedicalPerson() != null) {
            dto.setAssignedStaffId(entity.getMedicalPerson().getId());
            dto.setAssignedStaffName(entity.getMedicalPerson().getUser().getFullName());
            dto.setAssignedStaffEmail(entity.getMedicalPerson().getUser().getEmail());
        }

        if (entity.getAssignedBy() != null) {
            dto.setAssignedByUserEmail(entity.getAssignedBy().getEmail());
        }

        dto.setAssignedAt(entity.getAssignedAt());
        dto.setAssignmentNote(entity.getAssignmentNote());
        dto.setResponseMessage(entity.getResponseMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}