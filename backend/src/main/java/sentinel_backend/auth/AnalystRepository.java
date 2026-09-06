package sentinel_backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalystRepository extends JpaRepository<Analyst, Long> {

    long countByRoleAndEnabledTrue(AnalystRole role);
    Optional<Analyst> findByUsername(String username);
}