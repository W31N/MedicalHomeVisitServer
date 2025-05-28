package com.example.medicalhomevisit.service;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final AppointmentRequestRepository requestRepository;

    public List<Visit> getVisitsForToday() {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        return visitRepository.findByScheduledTimeBetween(startOfDay, endOfDay);
    }

    public Visit updateVisitStatus(UUID visitId, VisitStatus newStatus) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("Визит не найден"));

        visit.setStatus(newStatus);

        if (newStatus == VisitStatus.IN_PROGRESS) {
            visit.setActualStartTime(new Date());
        } else if (newStatus == VisitStatus.COMPLETED) {
            visit.setActualEndTime(new Date());
        }

        return visitRepository.save(visit);
    }
}
