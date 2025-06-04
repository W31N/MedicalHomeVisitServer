package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.*;
import com.example.medicalhomevisit.models.entities.*;
import com.example.medicalhomevisit.models.enums.RequestStatus;
import com.example.medicalhomevisit.models.enums.RequestType;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentRequestService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentRequestService.class);

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
        log.info("SERVICE: Attempting to create request by patient.");

        String currentUserEmail  = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("SERVICE: Current authenticated user email from SecurityContext: {}", currentUserEmail);

        UserEntity userEntity = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found for email: {}", currentUserEmail);
                    return new EntityNotFoundException("Пользователь не найден: " + currentUserEmail);
                });
        log.info("SERVICE: UserEntity found: ID={}, Email={}, Role={}", userEntity.getId(), userEntity.getEmail(), userEntity.getRole().getName());

        if (userEntity.getRole().getName() != UserRole.PATIENT) {
            log.warn("SERVICE: Access denied for user {}. Only PATIENT can create requests.", currentUserEmail);
            throw new AccessDeniedException("Только пациенты могут создавать заявки");
        }

        Patient patient = patientRepository.findByUser(userEntity)
                .orElseThrow(() -> {
                    log.error("SERVICE: Patient profile not found for User ID: {}", userEntity.getId());
                    return new EntityNotFoundException("Профиль пациента не найден для пользователя: " + userEntity.getEmail());
                });
        log.info("SERVICE: Patient profile found: ID={}", patient.getId());

        AppointmentRequest request = new AppointmentRequest();
        request.setPatient(patient);

        log.info("SERVICE: Assigning Patient ID {} to new request.", patient.getId());

        request.setAddress(createDto.getAddress());

        // Преобразуем строку в enum
        try {
            RequestType requestType = RequestType.valueOf(createDto.getRequestType());
            request.setRequestType(requestType);
        } catch (IllegalArgumentException e) {
            log.error("SERVICE: Invalid request type string: {}", createDto.getRequestType(), e);
            throw new IllegalArgumentException("Неверный тип заявки: " + createDto.getRequestType());
        }

        request.setSymptoms(createDto.getSymptoms());
        request.setAdditionalNotes(createDto.getAdditionalNotes());
        request.setStatus(RequestStatus.NEW);
        request.setPreferredDateTime(createDto.getPreferredDateTime());
        request.setAssignmentNote("");
        request.setResponseMessage("");

        AppointmentRequest savedRequest = requestRepository.save(request);
        log.info("SERVICE: Request created and saved with ID: {}", savedRequest.getId());
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
        log.info("SERVICE: getMyRequests() called."); // <-- ЛОГ: Начало метода
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("SERVICE: Current authenticated user email from SecurityContext: {}", currentUserEmail); // <-- ЛОГ: Email текущего пользователя

        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found for email (in getMyRequests): {}", currentUserEmail);
                    return new EntityNotFoundException("Пользователь не найден: " + currentUserEmail);
                });
        log.info("SERVICE: UserEntity found for getMyRequests: ID={}, Email={}, Role={}", currentUser.getId(), currentUser.getEmail(), currentUser.getRole().getName()); // <-- ЛОГ: Найденный UserEntity

        if (currentUser.getRole().getName() != UserRole.PATIENT) {
            log.warn("SERVICE: Access denied for user {} in getMyRequests. Only PATIENT can fetch their requests via this method.", currentUserEmail);
            throw new AccessDeniedException("Только пациенты могут получать свои заявки");
        }

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    log.error("SERVICE: Patient profile not found for User ID {} (in getMyRequests)", currentUser.getId());
                    return new EntityNotFoundException("Профиль пациента не найден для пользователя: " + currentUser.getEmail());
                });
        log.info("SERVICE: Patient profile found for getMyRequests: ID={}", patient.getId()); // <-- ЛОГ: Найденный Patient и его ID

        List<AppointmentRequest> requestsFromDb = requestRepository.findByPatient_Id(patient.getId());
        log.info("SERVICE: Found {} requests in DB for Patient ID: {}", requestsFromDb.size(), patient.getId()); // <-- ЛОГ: Количество заявок из БД

        List<AppointmentRequestDto> dtoList = requestsFromDb
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("SERVICE: Returning {} DTOs for getMyRequests.", dtoList.size()); // <-- ЛОГ: Количество DTO после конвертации

        return dtoList;
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
        // Здесь нет необходимости в логировании, если только нет проблем с маппингом
        AppointmentRequestDto dto = modelMapper.map(entity, AppointmentRequestDto.class);

        // ModelMapper может не справиться с вложенными полями типа Patient.User.FullName, если не настроен TypeMap
        // Ручное выставление некоторых полей после общего маппинга может быть надежнее или проще
        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId().toString());
            if (entity.getPatient().getUser() != null) {
                dto.setPatientName(entity.getPatient().getUser().getFullName());
            }
            dto.setPatientPhone(entity.getPatient().getPhoneNumber()); // Убедись, что Patient имеет phoneNumber
        }
        if (entity.getMedicalPerson() != null) {
            dto.setAssignedStaffId(entity.getMedicalPerson().getId().toString());
            if (entity.getMedicalPerson().getUser() != null) {
                dto.setAssignedStaffName(entity.getMedicalPerson().getUser().getFullName());
            }
        }
        if (entity.getAssignedBy() != null) {
            dto.setAssignedBy(entity.getAssignedBy().getId().toString()); // ID админа, назначившего заявку
        }
        // Enum в строки, если ModelMapper не настроен
        if (entity.getRequestType() != null) {
            dto.setRequestType(entity.getRequestType().name());
        }
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        return dto;
    }
}