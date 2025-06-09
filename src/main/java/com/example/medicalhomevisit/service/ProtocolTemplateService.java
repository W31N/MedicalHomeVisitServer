package com.example.medicalhomevisit.service;

import com.example.medicalhomevisit.dtos.ProtocolTemplateDto;
import com.example.medicalhomevisit.models.entities.ProtocolTemplate;
import com.example.medicalhomevisit.models.enums.UserRole;
import com.example.medicalhomevisit.repositories.ProtocolTemplateRepository;
import com.example.medicalhomevisit.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProtocolTemplateService {

    private static final Logger log = LoggerFactory.getLogger(ProtocolTemplateService.class);

    private ProtocolTemplateRepository protocolTemplateRepository;
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProtocolTemplateDto> getAllTemplates() {
        log.info("SERVICE: Getting all protocol templates");

        List<ProtocolTemplate> templates = protocolTemplateRepository.findAllOrderByName();
        log.info("SERVICE: Found {} protocol templates", templates.size());

        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProtocolTemplateDto getTemplateById(UUID templateId) {
        log.info("SERVICE: Getting protocol template by ID: {}", templateId);

        ProtocolTemplate template = protocolTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Шаблон протокола не найден: " + templateId));

        log.info("SERVICE: Found protocol template: {}", template.getName());
        return convertToDto(template);
    }

    public ProtocolTemplateDto createTemplate(ProtocolTemplateDto templateDto) {
        log.info("SERVICE: Creating new protocol template: {}", templateDto.getName());

        checkAdminAccess();

        if (protocolTemplateRepository.existsByNameIgnoreCase(templateDto.getName())) {
            throw new IllegalArgumentException("Шаблон с таким именем уже существует: " + templateDto.getName());
        }

        validateTemplate(templateDto);

        ProtocolTemplate template = new ProtocolTemplate();
        updateTemplateFromDto(template, templateDto);

        ProtocolTemplate savedTemplate = protocolTemplateRepository.save(template);
        log.info("SERVICE: Protocol template created successfully with ID: {}", savedTemplate.getId());

        return convertToDto(savedTemplate);
    }

    public ProtocolTemplateDto updateTemplate(UUID templateId, ProtocolTemplateDto templateDto) {
        log.info("SERVICE: Updating protocol template ID: {}", templateId);

        checkAdminAccess();

        ProtocolTemplate existingTemplate = protocolTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Шаблон протокола не найден: " + templateId));

        if (protocolTemplateRepository.existsByNameIgnoreCaseAndIdNot(templateDto.getName(), templateId)) {
            throw new IllegalArgumentException("Шаблон с таким именем уже существует: " + templateDto.getName());
        }

        validateTemplate(templateDto);

        updateTemplateFromDto(existingTemplate, templateDto);

        ProtocolTemplate updatedTemplate = protocolTemplateRepository.save(existingTemplate);
        log.info("SERVICE: Protocol template updated successfully: {}", updatedTemplate.getName());

        return convertToDto(updatedTemplate);
    }

    public void deleteTemplate(UUID templateId) {
        log.info("SERVICE: Deleting protocol template ID: {}", templateId);

        checkAdminAccess();

        ProtocolTemplate template = protocolTemplateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Шаблон протокола не найден: " + templateId));

        if (!template.getVisitProtocols().isEmpty()) {
            throw new IllegalStateException("Нельзя удалить шаблон, который используется в протоколах визитов");
        }

        protocolTemplateRepository.delete(template);
        log.info("SERVICE: Protocol template deleted successfully: {}", template.getName());
    }

    @Transactional(readOnly = true)
    public List<ProtocolTemplateDto> searchTemplates(String searchTerm) {
        log.info("SERVICE: Searching protocol templates with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTemplates();
        }

        List<ProtocolTemplate> templates = protocolTemplateRepository
                .findByNameOrDescriptionContainingIgnoreCase(searchTerm.trim());

        log.info("SERVICE: Found {} protocol templates matching search term", templates.size());

        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }



    private void checkAdminAccess() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        UserRole role = currentUser.getRole().getName();

        if (role != UserRole.ADMIN) {
            throw new AccessDeniedException("Доступ к управлению шаблонами протоколов разрешен только администраторам");
        }
    }

    private void validateTemplate(ProtocolTemplateDto templateDto) {
        if (templateDto.getName() == null || templateDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название шаблона не может быть пустым");
        }

        if (templateDto.getName().length() > 255) {
            throw new IllegalArgumentException("Название шаблона не может быть длиннее 255 символов");
        }
    }

    private void updateTemplateFromDto(ProtocolTemplate template, ProtocolTemplateDto dto) {
        template.setName(dto.getName().trim());
        template.setDescription(dto.getDescription());
        template.setComplaintsTemplate(dto.getComplaintsTemplate());
        template.setAnamnesisTemplate(dto.getAnamnesisTemplate());
        template.setObjectiveStatusTemplate(dto.getObjectiveStatusTemplate());
        template.setRecommendationsTemplate(dto.getRecommendationsTemplate());
        template.setRequiredVitals(dto.getRequiredVitals());
    }

    private ProtocolTemplateDto convertToDto(ProtocolTemplate entity) {
        ProtocolTemplateDto dto = new ProtocolTemplateDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setComplaintsTemplate(entity.getComplaintsTemplate());
        dto.setAnamnesisTemplate(entity.getAnamnesisTemplate());
        dto.setObjectiveStatusTemplate(entity.getObjectiveStatusTemplate());
        dto.setRecommendationsTemplate(entity.getRecommendationsTemplate());
        dto.setRequiredVitals(entity.getRequiredVitals());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    @Autowired
    public void setProtocolTemplateRepository(ProtocolTemplateRepository protocolTemplateRepository) {
        this.protocolTemplateRepository = protocolTemplateRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
