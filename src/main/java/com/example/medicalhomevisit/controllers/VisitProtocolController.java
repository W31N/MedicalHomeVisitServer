package com.example.medicalhomevisit.controllers;

import com.example.medicalhomevisit.dtos.ProtocolTemplateDto;
import com.example.medicalhomevisit.dtos.VisitProtocolDto;
import com.example.medicalhomevisit.service.ProtocolTemplateService;
import com.example.medicalhomevisit.service.VisitProtocolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/protocols")
public class VisitProtocolController {

    private static final Logger log = LoggerFactory.getLogger(VisitProtocolController.class);

    @Autowired
    private VisitProtocolService visitProtocolService;

    @Autowired
    private ProtocolTemplateService protocolTemplateService;

    /**
     * Получить протокол для визита
     * GET /api/protocols/visit/{visitId}
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<VisitProtocolDto> getProtocolForVisit(@PathVariable UUID visitId) {
        log.info("API: GET /api/protocols/visit/{} - Getting protocol for visit", visitId);

        VisitProtocolDto protocol = visitProtocolService.getProtocolForVisit(visitId);

        if (protocol == null) {
            log.info("API: No protocol found for visit {}", visitId);
            return ResponseEntity.notFound().build();
        }

        log.info("API: Returning protocol for visit {}", visitId);
        return ResponseEntity.ok(protocol);
    }

    /**
     * Сохранить протокол (создать или обновить)
     * POST /api/protocols
     * PUT /api/protocols
     */
    @PostMapping
    public ResponseEntity<VisitProtocolDto> createProtocol(@RequestBody VisitProtocolDto protocolDto) {
        log.info("API: POST /api/protocols - Creating protocol for visit {}", protocolDto.getVisitId());

        if (protocolDto.getVisitId() == null) {
            log.error("API: Visit ID is required");
            return ResponseEntity.badRequest().build();
        }

        VisitProtocolDto savedProtocol = visitProtocolService.saveProtocol(protocolDto);
        log.info("API: Protocol created successfully for visit {}", protocolDto.getVisitId());

        return ResponseEntity.ok(savedProtocol);
    }

    @PutMapping
    public ResponseEntity<VisitProtocolDto> updateProtocol(@RequestBody VisitProtocolDto protocolDto) {
        log.info("API: PUT /api/protocols - Updating protocol for visit {}", protocolDto.getVisitId());

        if (protocolDto.getVisitId() == null) {
            log.error("API: Visit ID is required");
            return ResponseEntity.badRequest().build();
        }

        VisitProtocolDto savedProtocol = visitProtocolService.saveProtocol(protocolDto);
        log.info("API: Protocol updated successfully for visit {}", protocolDto.getVisitId());

        return ResponseEntity.ok(savedProtocol);
    }

    /**
     * Сохранить протокол для конкретного визита
     * POST /api/protocols/visit/{visitId}
     * PUT /api/protocols/visit/{visitId}
     */
    @PostMapping("/visit/{visitId}")
    public ResponseEntity<VisitProtocolDto> createProtocolForVisit(
            @PathVariable UUID visitId,
            @RequestBody VisitProtocolDto protocolDto) {
        log.info("API: POST /api/protocols/visit/{} - Creating protocol", visitId);

        protocolDto.setVisitId(visitId); // Устанавливаем visitId из пути

        VisitProtocolDto savedProtocol = visitProtocolService.saveProtocol(protocolDto);
        log.info("API: Protocol created successfully for visit {}", visitId);

        return ResponseEntity.ok(savedProtocol);
    }

    @PutMapping("/visit/{visitId}")
    public ResponseEntity<VisitProtocolDto> updateProtocolForVisit(
            @PathVariable UUID visitId,
            @RequestBody VisitProtocolDto protocolDto) {
        log.info("API: PUT /api/protocols/visit/{} - Updating protocol", visitId);

        protocolDto.setVisitId(visitId); // Устанавливаем visitId из пути

        VisitProtocolDto savedProtocol = visitProtocolService.saveProtocol(protocolDto);
        log.info("API: Protocol updated successfully for visit {}", visitId);

        return ResponseEntity.ok(savedProtocol);
    }

    /**
     * Применить шаблон к протоколу
     * POST /api/protocols/visit/{visitId}/apply-template
     * Body: {"templateId": "uuid"}
     */
    @PostMapping("/visit/{visitId}/apply-template")
    public ResponseEntity<VisitProtocolDto> applyTemplate(
            @PathVariable UUID visitId,
            @RequestBody Map<String, String> templateRequest) {
        log.info("API: POST /api/protocols/visit/{}/apply-template - Applying template", visitId);

        String templateIdStr = templateRequest.get("templateId");
        if (templateIdStr == null || templateIdStr.trim().isEmpty()) {
            log.error("API: Template ID is required");
            return ResponseEntity.badRequest().build();
        }

        try {
            UUID templateId = UUID.fromString(templateIdStr);
            VisitProtocolDto updatedProtocol = visitProtocolService.applyTemplate(visitId, templateId);
            log.info("API: Template applied successfully to visit {}", visitId);
            return ResponseEntity.ok(updatedProtocol);
        } catch (IllegalArgumentException e) {
            log.error("API: Invalid template ID format: {}", templateIdStr);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удалить протокол
     * DELETE /api/protocols/visit/{visitId}
     */
    @DeleteMapping("/visit/{visitId}")
    public ResponseEntity<Void> deleteProtocol(@PathVariable UUID visitId) {
        log.info("API: DELETE /api/protocols/visit/{} - Deleting protocol", visitId);

        visitProtocolService.deleteProtocol(visitId);
        log.info("API: Protocol deleted successfully for visit {}", visitId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Частичное обновление полей протокола
     * PATCH /api/protocols/visit/{visitId}/field
     * Body: {"field": "complaints", "value": "новое значение"}
     */
    @PatchMapping("/visit/{visitId}/field")
    public ResponseEntity<VisitProtocolDto> updateProtocolField(
            @PathVariable UUID visitId,
            @RequestBody Map<String, String> fieldUpdate) {
        log.info("API: PATCH /api/protocols/visit/{}/field - Updating protocol field", visitId);

        String field = fieldUpdate.get("field");
        String value = fieldUpdate.get("value");

        if (field == null || field.trim().isEmpty()) {
            log.error("API: Field name is required");
            return ResponseEntity.badRequest().build();
        }

        // Получаем существующий протокол или создаем новый
        VisitProtocolDto protocol = visitProtocolService.getProtocolForVisit(visitId);
        if (protocol == null) {
            protocol = new VisitProtocolDto();
            protocol.setVisitId(visitId);
        }

        // Обновляем нужное поле
        switch (field.toLowerCase()) {
            case "complaints":
                protocol.setComplaints(value != null ? value : "");
                break;
            case "anamnesis":
                protocol.setAnamnesis(value != null ? value : "");
                break;
            case "objectivestatus":
                protocol.setObjectiveStatus(value != null ? value : "");
                break;
            case "diagnosis":
                protocol.setDiagnosis(value != null ? value : "");
                break;
            case "diagnosiscode":
                protocol.setDiagnosisCode(value != null ? value : "");
                break;
            case "recommendations":
                protocol.setRecommendations(value != null ? value : "");
                break;
            default:
                log.error("API: Unknown field: {}", field);
                return ResponseEntity.badRequest().build();
        }

        VisitProtocolDto updatedProtocol = visitProtocolService.saveProtocol(protocol);
        log.info("API: Protocol field {} updated successfully for visit {}", field, visitId);

        return ResponseEntity.ok(updatedProtocol);
    }

    /**
     * Обновление витальных показателей
     * PATCH /api/protocols/visit/{visitId}/vitals
     * Body: {"temperature": 36.6, "systolicBP": 120, "diastolicBP": 80, "pulse": 75}
     */
    @PatchMapping("/visit/{visitId}/vitals")
    public ResponseEntity<VisitProtocolDto> updateVitals(
            @PathVariable UUID visitId,
            @RequestBody Map<String, Object> vitalsUpdate) {
        log.info("API: PATCH /api/protocols/visit/{}/vitals - Updating vitals", visitId);

        // Получаем существующий протокол или создаем новый
        VisitProtocolDto protocol = visitProtocolService.getProtocolForVisit(visitId);
        if (protocol == null) {
            protocol = new VisitProtocolDto();
            protocol.setVisitId(visitId);
        }

        // Обновляем витальные показатели
        if (vitalsUpdate.containsKey("temperature")) {
            Object temp = vitalsUpdate.get("temperature");
            protocol.setTemperature(temp != null ? ((Number) temp).floatValue() : null);
        }

        if (vitalsUpdate.containsKey("systolicBP")) {
            Object systolic = vitalsUpdate.get("systolicBP");
            protocol.setSystolicBP(systolic != null ? ((Number) systolic).intValue() : null);
        }

        if (vitalsUpdate.containsKey("diastolicBP")) {
            Object diastolic = vitalsUpdate.get("diastolicBP");
            protocol.setDiastolicBP(diastolic != null ? ((Number) diastolic).intValue() : null);
        }

        if (vitalsUpdate.containsKey("pulse")) {
            Object pulse = vitalsUpdate.get("pulse");
            protocol.setPulse(pulse != null ? ((Number) pulse).intValue() : null);
        }

        VisitProtocolDto updatedProtocol = visitProtocolService.saveProtocol(protocol);
        log.info("API: Vitals updated successfully for visit {}", visitId);

        return ResponseEntity.ok(updatedProtocol);
    }

    /**
     * Получить все шаблоны протоколов
     * GET /api/protocol-templates
     * (Дублирует endpoint из ProtocolTemplateController для совместимости с мобилкой)
     */
    @GetMapping("/templates")
    public ResponseEntity<List<ProtocolTemplateDto>> getProtocolTemplates() {
        log.info("API: GET /api/protocols/templates - Getting all protocol templates");

        List<ProtocolTemplateDto> templates = protocolTemplateService.getAllTemplates();
        log.info("API: Returning {} protocol templates", templates.size());

        return ResponseEntity.ok(templates);
    }

    /**
     * Получить шаблон протокола по ID
     * GET /api/protocols/templates/{templateId}
     * (Дублирует endpoint из ProtocolTemplateController для совместимости с мобилкой)
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<ProtocolTemplateDto> getProtocolTemplateById(@PathVariable UUID templateId) {
        log.info("API: GET /api/protocols/templates/{} - Getting protocol template", templateId);

        try {
            ProtocolTemplateDto template = protocolTemplateService.getTemplateById(templateId);
            log.info("API: Returning protocol template: {}", template.getName());
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.info("API: Protocol template not found: {}", templateId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Поиск шаблонов протоколов
     * GET /api/protocols/templates/search?q={searchTerm}
     */
    @GetMapping("/templates/search")
    public ResponseEntity<List<ProtocolTemplateDto>> searchProtocolTemplates(
            @RequestParam(value = "q", required = false) String searchTerm) {
        log.info("API: GET /api/protocols/templates/search - Searching templates with term: {}", searchTerm);

        List<ProtocolTemplateDto> templates = protocolTemplateService.searchTemplates(searchTerm);
        log.info("API: Found {} templates matching search criteria", templates.size());

        return ResponseEntity.ok(templates);
    }
}
