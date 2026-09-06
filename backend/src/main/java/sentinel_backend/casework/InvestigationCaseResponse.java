package sentinel_backend.casework;

import java.time.Instant;

import sentinel_backend.event.EventStatus;
import sentinel_backend.event.EventType;
import sentinel_backend.event.Severity;

public record InvestigationCaseResponse(
        Long id,
        CaseStatus status,
        Instant createdAt,
        Long eventId,
        EventType eventType,
        Severity severity,
        EventStatus eventStatus,
        String source,
        String message,
        String ipAddress,
        Instant eventTimestamp) {
}