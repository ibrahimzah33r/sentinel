package sentinel_backend.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SecurityEventRequest(
        @NotBlank String source,
        @NotNull EventType eventType,
        @NotNull Severity severity,
        @NotBlank String message,
        String ipAddress) {
}