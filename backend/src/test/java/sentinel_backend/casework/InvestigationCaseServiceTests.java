package sentinel_backend.casework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sentinel_backend.error.InvalidOperationException;
import sentinel_backend.error.ResourceConflictException;
import sentinel_backend.error.ResourceNotFoundException;
import sentinel_backend.event.EventStatus;
import sentinel_backend.event.EventType;
import sentinel_backend.event.SecurityEvent;
import sentinel_backend.event.SecurityEventRepository;
import sentinel_backend.event.Severity;

@ExtendWith(MockitoExtension.class)
class InvestigationCaseServiceTests {

    @Mock
    private InvestigationCaseRepository repository;

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private SecurityEvent event;

    @InjectMocks
    private InvestigationCaseService service;

    @Test
    void shouldRejectMissingEvent() {
        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createFromEvent(999L));

        verify(repository, never())
                .save(any(InvestigationCase.class));
    }

    @Test
    void shouldRejectNonEscalatedEvent() {
        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(event.getStatus())
                .thenReturn(EventStatus.REVIEWED);

        assertThrows(
                InvalidOperationException.class,
                () -> service.createFromEvent(1L));

        verify(repository, never())
                .save(any(InvestigationCase.class));
    }

    @Test
    void shouldRejectDuplicateCase() {
        InvestigationCase existingCase = org.mockito.Mockito.mock(InvestigationCase.class);

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(event.getStatus())
                .thenReturn(EventStatus.ESCALATED);

        when(repository.findBySecurityEventId(1L))
                .thenReturn(Optional.of(existingCase));

        assertThrows(
                ResourceConflictException.class,
                () -> service.createFromEvent(1L));

        verify(repository, never())
                .save(any(InvestigationCase.class));
    }

    @Test
    void shouldCreateCaseFromEscalatedEvent() {
        Instant timestamp = Instant.now();

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(event.getStatus())
                .thenReturn(EventStatus.ESCALATED);

        when(repository.findBySecurityEventId(1L))
                .thenReturn(Optional.empty());

        when(event.getId())
                .thenReturn(1L);

        when(event.getEventType())
                .thenReturn(EventType.FAILED_LOGIN);

        when(event.getSeverity())
                .thenReturn(Severity.HIGH);

        when(event.getSource())
                .thenReturn("ids-01");

        when(event.getMessage())
                .thenReturn("Repeated failed login attempts");

        when(event.getIpAddress())
                .thenReturn("192.168.1.100");

        when(event.getTimestamp())
                .thenReturn(timestamp);

        when(repository.save(any(InvestigationCase.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InvestigationCaseResponse response = service.createFromEvent(1L);

        assertEquals(CaseStatus.OPEN, response.status());
        assertEquals(1L, response.eventId());
        assertEquals(
                EventType.FAILED_LOGIN,
                response.eventType());
        assertEquals(Severity.HIGH, response.severity());
        assertEquals(
                EventStatus.ESCALATED,
                response.eventStatus());
        assertEquals("ids-01", response.source());
        assertNotNull(response.createdAt());

        verify(repository)
                .save(any(InvestigationCase.class));
    }

    @Test
    void shouldRejectMissingCaseDuringStatusUpdate() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateStatus(
                        999L,
                        CaseStatus.CLOSED));
    }
}