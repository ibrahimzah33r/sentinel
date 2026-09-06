package sentinel_backend.casework;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import sentinel_backend.error.InvalidOperationException;
import sentinel_backend.error.ResourceConflictException;
import sentinel_backend.error.ResourceNotFoundException;
import sentinel_backend.event.EventStatus;
import sentinel_backend.event.SecurityEvent;
import sentinel_backend.event.SecurityEventRepository;

@Service
public class InvestigationCaseService {

    private final InvestigationCaseRepository repository;
    private final SecurityEventRepository eventRepository;

    public InvestigationCaseService(
            InvestigationCaseRepository repository,
            SecurityEventRepository eventRepository) {
        this.repository = repository;
        this.eventRepository = eventRepository;
    }

    public InvestigationCaseResponse createFromEvent(
            Long eventId) {
        SecurityEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found"));

        if (event.getStatus() != EventStatus.ESCALATED) {
            throw new InvalidOperationException(
                    "Only escalated events can become cases");
        }

        if (repository
                .findBySecurityEventId(eventId)
                .isPresent()) {
            throw new ResourceConflictException(
                    "A case already exists for this event");
        }

        InvestigationCase investigationCase = new InvestigationCase(
                event,
                Instant.now());

        InvestigationCase savedCase = repository.save(investigationCase);

        return toResponse(savedCase);
    }

    public List<InvestigationCaseResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InvestigationCaseResponse toResponse(
            InvestigationCase investigationCase) {
        SecurityEvent event = investigationCase.getSecurityEvent();

        return new InvestigationCaseResponse(
                investigationCase.getId(),
                investigationCase.getStatus(),
                investigationCase.getCreatedAt(),
                event.getId(),
                event.getEventType(),
                event.getSeverity(),
                event.getStatus(),
                event.getSource(),
                event.getMessage(),
                event.getIpAddress(),
                event.getTimestamp());
    }

    public InvestigationCaseResponse updateStatus(
            Long id,
            CaseStatus status) {
        InvestigationCase investigationCase = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Case not found"));

        if (status == CaseStatus.CLOSED) {
            investigationCase.close();
        } else {
            investigationCase.reopen();
        }

        InvestigationCase savedCase = repository.save(investigationCase);

        return toResponse(savedCase);
    }

}