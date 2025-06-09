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

    private ModelMapper modelMapper;
    private AppointmentRequestRepository requestRepository;
    private PatientRepository patientRepository;
    private UserRepository userRepository;
    private MedicalPersonRepository medicalPersonRepository;
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


    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getRequestsForPatient(UUID patientId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        if (role == UserRole.ADMIN || role == UserRole.MEDICAL_STAFF) {
            return requestRepository.findByPatient_Id(patientId)
                    .stream().map(this::convertToDto).collect(Collectors.toList());
        }

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


    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getMyRequests() {
        log.info("SERVICE: getMyRequests() called.");
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("SERVICE: Current authenticated user email from SecurityContext: {}", currentUserEmail);

        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.error("SERVICE: User not found for email (in getMyRequests): {}", currentUserEmail);
                    return new EntityNotFoundException("Пользователь не найден: " + currentUserEmail);
                });
        log.info("SERVICE: UserEntity found for getMyRequests: ID={}, Email={}, Role={}", currentUser.getId(), currentUser.getEmail(), currentUser.getRole().getName());

        if (currentUser.getRole().getName() != UserRole.PATIENT) {
            log.warn("SERVICE: Access denied for user {} in getMyRequests. Only PATIENT can fetch their requests via this method.", currentUserEmail);
            throw new AccessDeniedException("Только пациенты могут получать свои заявки");
        }

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> {
                    log.error("SERVICE: Patient profile not found for User ID {} (in getMyRequests)", currentUser.getId());
                    return new EntityNotFoundException("Профиль пациента не найден для пользователя: " + currentUser.getEmail());
                });
        log.info("SERVICE: Patient profile found for getMyRequests: ID={}", patient.getId());

        List<AppointmentRequest> requestsFromDb = requestRepository.findByPatient_Id(patient.getId());
        log.info("SERVICE: Found {} requests in DB for Patient ID: {}", requestsFromDb.size(), patient.getId());

        List<AppointmentRequestDto> dtoList = requestsFromDb
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("SERVICE: Returning {} DTOs for getMyRequests.", dtoList.size());

        return dtoList;
    }


    @Transactional(readOnly = true)
    public List<AppointmentRequestDto> getAllActiveRequests() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();
        if (role != UserRole.ADMIN) {
            throw new AccessDeniedException("Только администратор или диспетчер могут просматривать все заявки");
        }

        List<RequestStatus> activeStatuses = List.of(
                RequestStatus.NEW, RequestStatus.PENDING,
                RequestStatus.ASSIGNED, RequestStatus.SCHEDULED
        );

        return requestRepository.findByStatusIn(activeStatuses)
                .stream().map(this::convertToDto).collect(Collectors.toList());
    }


    public AppointmentRequestDto assignStaffToRequest(UUID requestId, AssignStaffToRequestDto assignDto) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();
        if (role != UserRole.ADMIN) {
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

        createVisitFromRequest(updatedRequest);

        request.setStatus(RequestStatus.SCHEDULED);
        updatedRequest = requestRepository.save(request);

        return convertToDto(updatedRequest);
    }


    public AppointmentRequestDto updateRequestStatus(UUID requestId, UpdateRequestStatusDto statusDto) {
        AppointmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена: " + requestId));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

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

        if (role == UserRole.ADMIN) {
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
        AppointmentRequestDto dto = modelMapper.map(entity, AppointmentRequestDto.class);
        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId().toString());
            if (entity.getPatient().getUser() != null) {
                dto.setPatientName(entity.getPatient().getUser().getFullName());
            }
            dto.setPatientPhone(entity.getPatient().getPhoneNumber());
        }
        if (entity.getMedicalPerson() != null) {
            dto.setAssignedStaffId(entity.getMedicalPerson().getId().toString());
            if (entity.getMedicalPerson().getUser() != null) {
                dto.setAssignedStaffName(entity.getMedicalPerson().getUser().getFullName());
            }
        }
        if (entity.getAssignedBy() != null) {
            dto.setAssignedBy(entity.getAssignedBy().getId().toString());
        }
        if (entity.getRequestType() != null) {
            dto.setRequestType(entity.getRequestType().name());
        }
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        return dto;
    }

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Autowired
    public void setRequestRepository(AppointmentRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Autowired
    public void setPatientRepository(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
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
    public void setVisitRepository(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }
}