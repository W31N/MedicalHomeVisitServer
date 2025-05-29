//package com.example.medicalhomevisit.service;
//
//import com.example.medicalhomevisit.dtos.*;
//import com.example.medicalhomevisit.models.entities.*;
//import com.example.medicalhomevisit.models.enums.RequestStatus;
//import com.example.medicalhomevisit.models.enums.UserRole;
//import com.example.medicalhomevisit.models.enums.VisitStatus; // Для создания визита
//import com.example.medicalhomevisit.repositories.*;
//// import com.example.medicalhomevisit.exception.*; // Ваши кастомные исключения
//import jakarta.persistence.EntityNotFoundException;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class AppointmentRequestService {
//
//    @Autowired
//    private ModelMapper modelMapper; // <-- ВНЕДРЯЕМ ModelMapper
//    @Autowired
//    private AppointmentRequestRepository requestRepository;
//    @Autowired
//    private PatientRepository patientRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private MedicalPersonRepository medicalPersonRepository; // Нужен для назначения
//    @Autowired
//    private VisitRepository visitRepository; // Нужен для создания визита при назначении
//
//    // Метод для создания заявки пациентом
//    public AppointmentRequestDto createRequestByPatient(CreateAppointmentRequestDto createDto) {
//        String currentPatientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        UserEntity userEntity = userRepository.findByEmail(currentPatientEmail)
//                .orElseThrow(() -> new RuntimeException("Текущий пользователь-пациент не найден")); // Замените на UserNotFoundException
//
//        Patient patient = patientRepository.findByUser(userEntity)
//                .orElseThrow(() -> new RuntimeException("Профиль пациента не найден для пользователя: " + currentPatientEmail)); // PatientProfileNotFoundException
//
//        AppointmentRequest request = new AppointmentRequest();
//
//        request.setPatient(patient);
//        request.setRequestType(createDto.getRequestType());
//        request.setSymptoms(createDto.getSymptoms());
//        request.setAdditionalNotes(createDto.getAdditionalNotes());
//        request.setStatus(RequestStatus.NEW);
//        request.setMedicalPerson(null);
//        request.setAssignedBy(null);
//        request.setPreferredDateTime(createDto.getPreferredDateTime());
//        request.setAssignmentNote("");
//        request.setResponseMessage("");
//
//        AppointmentRequest savedRequest = requestRepository.save(request);
//        return convertToDto(savedRequest);
//    }
//
//    // Метод для получения всех активных заявок (для админа/диспетчера)
//    @Transactional(readOnly = true)
//    public List<AppointmentRequestDto> getAllActiveRequests() {
//        List<RequestStatus> activeStatuses = List.of(
//                RequestStatus.NEW, RequestStatus.PENDING,
//                RequestStatus.ASSIGNED, RequestStatus.SCHEDULED
//        );
//        return requestRepository.findByStatusIn(activeStatuses)
//                .stream().map(this::convertToDto).collect(Collectors.toList());
//    }
//
//    // Метод для получения заявок конкретного пациента с проверкой прав
//    @Transactional(readOnly = true)
//    public List<AppointmentRequestDto> getRequestsForPatient(UUID patientId) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
//            throw new AccessDeniedException("Пользователь не аутентифицирован.");
//        }
//
//        String currentUserEmail = authentication.getName();
//        UserEntity currentUserEntity = userRepository.findByEmail(currentUserEmail)
//                .orElseThrow(() -> new UsernameNotFoundException("Текущий пользователь не найден: " + currentUserEmail));
//
//        UserRole currentUserRole = currentUserEntity.getRole().getName();
//
//        // Проверяем, существует ли вообще пациент с таким patientId
//        Patient targetPatient = patientRepository.findById(patientId)
//                .orElseThrow(() -> new EntityNotFoundException("Пациент с ID " + patientId + " не найден."));
//
//        // Логика доступа
//        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.MEDICAL_STAFF) {
//            // Админ и Медработник могут видеть заявки любого пациента
//            return requestRepository.findByPatient_Id(patientId)
//                    .stream().map(this::convertToDto).collect(Collectors.toList());
//        } else if (currentUserRole == UserRole.PATIENT) {
//            // Пациент может видеть только свои заявки
//            // Получаем профиль пациента для текущего UserEntity
//            Patient authenticatedPatientProfile = patientRepository.findByUser(currentUserEntity)
//                    .orElseThrow(() -> new EntityNotFoundException("Профиль пациента не найден для текущего пользователя."));
//
//            if (authenticatedPatientProfile.getId().equals(patientId)) {
//                // ID пациента из запроса совпадает с ID аутентифицированного пациента
//                return requestRepository.findByPatient_Id(patientId)
//                        .stream().map(this::convertToDto).collect(Collectors.toList());
//            } else {
//                // Пациент пытается получить доступ к чужим заявкам
//                throw new AccessDeniedException("Доступ запрещен: Пациент может просматривать только свои заявки.");
//            }
//        } else {
//            // Другие роли (например, Диспетчер, если он не добавлен выше) по умолчанию не имеют доступа
//            // или вы можете добавить для них отдельную логику
//            throw new AccessDeniedException("У вас нет прав на просмотр заявок этого пациента.");
//        }
//    }
//
//    // Метод для получения заявок, назначенных конкретному медработнику
//    @Transactional(readOnly = true)
//    public List<AppointmentRequestDto> getRequestsForStaff(UUID staffId) {
//        // Здесь нужна проверка, что текущий пользователь - это этот медработник или админ/диспетчер
//        return requestRepository.findByAssignedStaff_Id(staffId)
//                .stream().map(this::convertToDto).collect(Collectors.toList());
//    }
//
//
//    // Метод для назначения медработника на заявку (для админа/диспетчера)
//    public AppointmentRequestDto assignStaffToRequest(UUID requestId, AssignStaffToRequestDto assignDto) {
//        AppointmentRequest request = requestRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Заявка с ID " + requestId + " не найдена")); // RequestNotFoundException
//
//        MedicalPerson staff = medicalPersonRepository.findById(assignDto.getStaffId())
//                .orElseThrow(() -> new RuntimeException("Медработник с ID " + assignDto.getStaffId() + " не найден")); // MedicalPersonNotFoundException
//
//        String assignerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        UserEntity assignerUser = userRepository.findByEmail(assignerEmail)
//                .orElseThrow(() -> new RuntimeException("Пользователь-диспетчер/админ не найден"));
//
//
//        request.setAssignedStaff(staff);
//        request.setAssignedBy(assignerUser);
//        request.setAssignedAt(new Date());
//        request.setAssignmentNote(assignDto.getAssignmentNote());
//        request.setStatus(RequestStatus.ASSIGNED); // Или SCHEDULED, если сразу планируем
//        // request.setUpdatedAt(new Date()); // Автоматически через @UpdateTimestamp
//
//        AppointmentRequest updatedRequest = requestRepository.save(request);
//
//        // Автоматическое создание визита при назначении заявки
//        if (request.getStatus() == RequestStatus.ASSIGNED || request.getStatus() == RequestStatus.SCHEDULED) {
//            // Проверяем, не создан ли уже визит для этой заявки
//            if (visitRepository.findByAppointmentRequest(updatedRequest).isEmpty()) {
//                Visit visit = new Visit();
//                visit.setAppointmentRequest(updatedRequest);
//                visit.setScheduledTime(updatedRequest.getPreferredDate() != null ? updatedRequest.getPreferredDate() : new Date()); // Уточнить логику времени
//                visit.setStatus(VisitStatus.PLANNED); // Статус визита
//                // Адрес и причина могут браться из заявки
//                // visit.setAddress(updatedRequest.getAddress());
//                // visit.setReasonForVisit(updatedRequest.getSymptoms());
//                // visit.setAssignedStaff(updatedRequest.getAssignedStaff()); // Если в Visit есть прямая ссылка на MedicalPerson
//
//                visitRepository.save(visit);
//
//                // Можно обновить статус заявки на SCHEDULED, если она была ASSIGNED
//                if (request.getStatus() == RequestStatus.ASSIGNED) {
//                    request.setStatus(RequestStatus.SCHEDULED);
//                    requestRepository.save(request);
//                }
//            }
//        }
//        return convertToDto(updatedRequest);
//    }
//
//    // Метод для обновления статуса заявки (пациентом для отмены, или медработником/админом)
//    public AppointmentRequestDto updateRequestStatus(UUID requestId, UpdateRequestStatusDto statusDto) {
//        AppointmentRequest request = requestRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Заявка с ID " + requestId + " не найдена"));
//
//        // TODO: Добавить проверки прав на изменение статуса в зависимости от текущего пользователя и нового статуса
//        // Например, пациент может отменить только свою заявку в статусе NEW или PENDING
//        // Медработник может менять статус связанного с ним визита (что обновит заявку)
//        // Админ/диспетчер могут иметь больше прав.
//
//        request.setStatus(statusDto.getStatus());
//        if (statusDto.getResponseMessage() != null) {
//            request.setResponseMessage(statusDto.getResponseMessage());
//        }
//        // request.setUpdatedAt(new Date()); // Автоматически
//
//        AppointmentRequest updatedRequest = requestRepository.save(request);
//        return convertToDto(updatedRequest);
//    }
//
//
//    // Конвертер из Entity в DTO
//    private AppointmentRequestDto convertToDto(AppointmentRequest entity) {
//        AppointmentRequestDto dto = new AppointmentRequestDto();
//        dto.setId(entity.getId());
//        if (entity.getPatient() != null) {
//            dto.setPatientId(entity.getPatient().getId());
//            // dto.setPatientName(entity.getPatient().getUser().getFullName()); // Если Patient связан с UserEntity
//            dto.setPatientName(entity.getPatientName()); // Если денормализовано в заявке
//            dto.setPatientPhone(entity.getPatientPhone()); // Если денормализовано в заявке
//        }
//        dto.setAddress(entity.getAddress());
//        dto.setRequestType(entity.getRequestType());
//        dto.setSymptoms(entity.getSymptoms());
//        dto.setAdditionalNotes(entity.getAdditionalNotes());
//        dto.setPreferredDate(entity.getPreferredDate());
//        dto.setPreferredTimeRange(entity.getPreferredTimeRange());
//        dto.setStatus(entity.getStatus());
//        if (entity.getAssignedStaff() != null) {
//            dto.setAssignedStaffId(entity.getAssignedStaff().getId());
//            dto.setAssignedStaffName(entity.getAssignedStaff().getUser().getFullName()); // Если MedicalPerson связан с UserEntity
//        }
//        if (entity.getAssignedBy() != null) {
//            dto.setAssignedByUserId(entity.getAssignedBy().getId());
//            dto.setAssignedByUserName(entity.getAssignedBy().getFullName());
//        }
//        dto.setAssignedAt(entity.getAssignedAt());
//        dto.setAssignmentNote(entity.getAssignmentNote());
//        dto.setResponseMessage(entity.getResponseMessage());
//        dto.setUrgencyLevel(entity.getUrgencyLevel());
//        dto.setPriority(entity.getPriority());
//        dto.setCreatedAt(entity.getCreatedAt());
//        dto.setUpdatedAt(entity.getUpdatedAt());
//        return dto;
//    }
//}