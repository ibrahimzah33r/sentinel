package sentinel_backend.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

import sentinel_backend.error.InvalidOperationException;
import sentinel_backend.error.ResourceNotFoundException;
import sentinel_backend.error.ResourceConflictException;

@Service
public class AuthService {

        private final AnalystRepository analystRepository;
        private final PasswordEncoder passwordEncoder;

        public AuthService(
                        AnalystRepository analystRepository,
                        PasswordEncoder passwordEncoder) {
                this.analystRepository = analystRepository;
                this.passwordEncoder = passwordEncoder;
        }

        public LoginResponse login(LoginRequest request) {
                Analyst analyst = analystRepository.findByUsername(request.username())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

                if (!analyst.isEnabled()) {
                        throw new IllegalArgumentException(
                                        "Invalid credentials");
                }
                if (!passwordEncoder.matches(request.password(), analyst.getPasswordHash())) {
                        throw new IllegalArgumentException("Invalid credentials");
                }

                return new LoginResponse(
                                analyst.getId(),
                                analyst.getUsername(),
                                analyst.getRole());
        }

        public AnalystResponse createAnalyst(
                        CreateAnalystRequest request) {
                if (analystRepository
                                .findByUsername(request.username())
                                .isPresent()) {
                        throw new ResourceConflictException(
                                        "Username already exists");
                }

                Analyst analyst = new Analyst();

                analyst.setUsername(request.username());

                analyst.setPasswordHash(
                                passwordEncoder.encode(request.password()));

                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                return new AnalystResponse(
                                savedAnalyst.getId(),
                                savedAnalyst.getUsername(),
                                savedAnalyst.getRole(),
                                savedAnalyst.isEnabled());
        }

        public AnalystResponse resetAnalystPassword(
                        Long id,
                        ResetAnalystPasswordRequest request) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                analyst.setPasswordHash(
                                passwordEncoder.encode(request.password()));

                Analyst savedAnalyst = analystRepository.save(analyst);

                return new AnalystResponse(
                                savedAnalyst.getId(),
                                savedAnalyst.getUsername(),
                                savedAnalyst.getRole(),
                                savedAnalyst.isEnabled());
        }

        public AnalystResponse setAnalystEnabled(
                        Long id,
                        boolean enabled) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                if (!enabled
                                && analyst.getRole() == AnalystRole.ADMIN
                                && analystRepository
                                                .countByRoleAndEnabledTrue(
                                                                AnalystRole.ADMIN) <= 1) {
                        throw new InvalidOperationException(
                                        "Cannot disable the last enabled admin");
                }

                analyst.setEnabled(enabled);

                Analyst saved = analystRepository.save(analyst);

                return new AnalystResponse(
                                saved.getId(),
                                saved.getUsername(),
                                saved.getRole(),
                                saved.isEnabled());
        }

        public List<AnalystResponse> getAnalysts() {
                return analystRepository
                                .findAll()
                                .stream()
                                .map(analyst -> new AnalystResponse(
                                                analyst.getId(),
                                                analyst.getUsername(),
                                                analyst.getRole(),
                                                analyst.isEnabled()))
                                .toList();
        }

        public LoginResponse getCurrentUser(String username) {
                Analyst analyst = analystRepository
                                .findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Authenticated analyst not found"));

                return new LoginResponse(
                                analyst.getId(),
                                analyst.getUsername(),
                                analyst.getRole());
        }

        public void deleteAnalyst(Long id) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                if (analyst.getRole() == AnalystRole.ADMIN
                                && analyst.isEnabled()
                                && analystRepository.countByRoleAndEnabledTrue(
                                        AnalystRole.ADMIN) <= 1) {
                        throw new InvalidOperationException(
                                        "Cannot delete the last enabled admin");
                }

                analystRepository.delete(analyst);
        }

        public AnalystResponse setAnalystRole(
                        Long id,
                        AnalystRole role) {
                Analyst analyst = analystRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Analyst not found"));

                if (analyst.getRole() == AnalystRole.ADMIN
                                && role != AnalystRole.ADMIN
                                && analyst.isEnabled()
                                && analystRepository
                                                .countByRoleAndEnabledTrue(
                                                                AnalystRole.ADMIN) <= 1) {
                        throw new InvalidOperationException(
                                        "Cannot demote the last enabled admin");
                }

                analyst.setRole(role);

                Analyst saved = analystRepository.save(analyst);

                return new AnalystResponse(
                                saved.getId(),
                                saved.getUsername(),
                                saved.getRole(),
                                saved.isEnabled());
        }
}