package sentinel_backend.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import java.util.List;


@RestController
@RequestMapping("/api/events")
public class SecurityEventController {

    private final SecurityEventService service;

    public SecurityEventController(SecurityEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<SecurityEventResponse> getEvents(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) EventType eventType) {

        if (severity != null && eventType != null) {
            return service.getEventsBySeverityAndEventType(severity, eventType);
        }

        if (severity != null) {
            return service.getEventsBySeverity(severity);
        }

        if (eventType != null) {
            return service.getEventsByEventType(eventType);
        }

        return service.getAllEvents();
    }

    @PostMapping
    public SecurityEventResponse createEvent(
            @Valid @RequestBody SecurityEventRequest request) {

        return service.saveEvent(request);
    }

    @GetMapping("/{id}")
    public SecurityEventResponse getEventById(@PathVariable Long id) {
        return service.getEventById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
    }

    @PutMapping("/{id}")
    public SecurityEventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody SecurityEventRequest request) {

        return service.updateEvent(id, request);
    }

    @GetMapping("/page")
    public Page<SecurityEventResponse> getEventsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100");
        }
        return service.getEventsPage(page, size);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SecurityEventResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        return ResponseEntity.ok(
                service.updateStatus(id, status));
    }
}