package sentinel_backend.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import sentinel_backend.error.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class SecurityEventServiceTests {

    @Mock
    private SecurityEventRepository repository;

    @Mock
    private SecurityEvent event;

    @InjectMocks
    private SecurityEventService service;

    @Test
    void shouldRejectMissingEventDuringStatusUpdate() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateStatus(
                        999L,
                        EventStatus.REVIEWED));

        verify(repository, never())
                .save(event);
    }

    @Test
    void shouldUpdateEventStatus() {
        Instant timestamp = Instant.now();

        when(repository.findById(1L))
                .thenReturn(Optional.of(event));

        when(repository.save(event))
                .thenReturn(event);

        when(event.getId())
                .thenReturn(1L);

        when(event.getSource())
                .thenReturn("ids-01");

        when(event.getEventType())
                .thenReturn(EventType.FAILED_LOGIN);

        when(event.getSeverity())
                .thenReturn(Severity.HIGH);

        when(event.getStatus())
                .thenReturn(EventStatus.REVIEWED);

        when(event.getMessage())
                .thenReturn("Repeated failed login attempts");

        when(event.getIpAddress())
                .thenReturn("192.168.1.100");

        when(event.getTimestamp())
                .thenReturn(timestamp);

        SecurityEventResponse response = service.updateStatus(
                1L,
                EventStatus.REVIEWED);

        verify(event)
                .setStatus(EventStatus.REVIEWED);

        verify(repository)
                .save(event);

        assertEquals(1L, response.id());
        assertEquals("ids-01", response.source());
        assertEquals(
                EventType.FAILED_LOGIN,
                response.eventType());
        assertEquals(Severity.HIGH, response.severity());
        assertEquals(
                EventStatus.REVIEWED,
                response.status());
        assertEquals(
                "Repeated failed login attempts",
                response.message());
        assertEquals(
                "192.168.1.100",
                response.ipAddress());
        assertEquals(timestamp, response.timestamp());
    }
}