package sentinel_backend.casework;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public enum CaseStatus {
    OPEN,
    CLOSED
}