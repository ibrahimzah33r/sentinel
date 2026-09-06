package sentinel_backend.casework;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import sentinel_backend.event.SecurityEvent;

@Entity
@Table(name = "investigation_case")
public class InvestigationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "security_event_id", nullable = false, unique = true)
    private SecurityEvent securityEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.OPEN;

    @Column(nullable = false)
    private Instant createdAt;

    protected InvestigationCase() {
    }

    public InvestigationCase(
            SecurityEvent securityEvent,
            Instant createdAt) {
        this.securityEvent = securityEvent;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public SecurityEvent getSecurityEvent() {
        return securityEvent;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void close() {
        this.status = CaseStatus.CLOSED;
    }

    public void reopen() {
        this.status = CaseStatus.OPEN;
    }
}