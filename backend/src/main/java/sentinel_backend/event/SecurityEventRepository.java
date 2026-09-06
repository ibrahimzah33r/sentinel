package sentinel_backend.event;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
        List<SecurityEvent> findBySeverity(Severity severity);

        List<SecurityEvent> findByEventType(EventType eventType);

        List<SecurityEvent> findBySeverityAndEventType(
                        Severity severity,
                        EventType eventType);

        List<SecurityEvent> findAllByOrderByTimestampDesc();

        List<SecurityEvent> findBySeverityOrderByTimestampDesc(
                        Severity severity);

        List<SecurityEvent> findByEventTypeOrderByTimestampDesc(
                        EventType eventType);

        List<SecurityEvent> findBySeverityAndEventTypeOrderByTimestampDesc(
                        Severity severity,
                        EventType eventType);

}