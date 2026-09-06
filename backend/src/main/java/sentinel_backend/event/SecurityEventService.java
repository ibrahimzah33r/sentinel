package sentinel_backend.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sentinel_backend.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sentinel_backend.alert.AlertWebhookClient;

@Service
public class SecurityEventService {

    private final SecurityEventRepository repository;
    private final AlertWebhookClient alertWebhookClient;
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventService.class);

    public SecurityEventService(
            SecurityEventRepository repository,
            AlertWebhookClient alertWebhookClient) {
        this.repository = repository;
        this.alertWebhookClient = alertWebhookClient;
    }

    public List<SecurityEventResponse> getAllEvents() {
        return repository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SecurityEventResponse saveEvent(SecurityEventRequest request) {
        SecurityEvent event = fromRequest(request);
        SecurityEvent savedEvent = repository.save(event);
        SecurityEventResponse response = toResponse(savedEvent);

        logger.info(
                "Security event created: id={}, type={}, severity={}, source={}",
                savedEvent.getId(),
                savedEvent.getEventType(),
                savedEvent.getSeverity(),
                savedEvent.getSource());

        if (savedEvent.getSeverity() == Severity.CRITICAL) {
            alertWebhookClient.sendCriticalAlert(response);
        }

        return response;
    }

    public void deleteEvent(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Security event not found");
        }

        repository.deleteById(id);
    }

    public SecurityEventResponse updateEvent(
            Long id,
            SecurityEventRequest request) {

        SecurityEvent existingEvent = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Security event not found"));

        existingEvent.setSource(request.source());
        existingEvent.setEventType(request.eventType());
        existingEvent.setSeverity(request.severity());
        existingEvent.setMessage(request.message());
        existingEvent.setIpAddress(request.ipAddress());

        SecurityEvent savedEvent = repository.save(existingEvent);

        return toResponse(savedEvent);
    }

    public SecurityEventResponse getEventById(Long id) {
        SecurityEvent event = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Security event not found"));

        return toResponse(event);
    }

    public List<SecurityEventResponse> getEventsBySeverity(Severity severity) {
        return repository.findBySeverityOrderByTimestampDesc(severity)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SecurityEventResponse> getEventsByEventType(EventType eventType) {
        return repository.findByEventTypeOrderByTimestampDesc(eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SecurityEventResponse> getEventsBySeverityAndEventType(
            Severity severity,
            EventType eventType) {

        return repository
                .findBySeverityAndEventTypeOrderByTimestampDesc(severity, eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<SecurityEventResponse> getEventsPage(int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by("timestamp").descending());

        return repository.findAll(pageRequest)
                .map(this::toResponse);
    }

    private SecurityEventResponse toResponse(SecurityEvent event) {
        return new SecurityEventResponse(
                event.getId(),
                event.getSource(),
                event.getEventType(),
                event.getSeverity(),
                event.getStatus(),
                event.getMessage(),
                event.getIpAddress(),
                event.getTimestamp());
    }

    private SecurityEvent fromRequest(SecurityEventRequest request) {
        SecurityEvent event = new SecurityEvent();

        event.setSource(request.source());
        event.setEventType(request.eventType());
        event.setSeverity(request.severity());
        event.setMessage(request.message());
        event.setIpAddress(request.ipAddress());

        return event;
    }

    public SecurityEventResponse updateStatus(
            Long id,
            EventStatus status) {
        SecurityEvent event = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found"));

        event.setStatus(status);

        SecurityEvent savedEvent = repository.save(event);

        return toResponse(savedEvent);
    }
}