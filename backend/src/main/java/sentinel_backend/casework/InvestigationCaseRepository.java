package sentinel_backend.casework;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationCaseRepository
        extends JpaRepository<InvestigationCase, Long> {

    Optional<InvestigationCase> findBySecurityEventId(Long securityEventId);
}