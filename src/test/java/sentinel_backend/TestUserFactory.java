package sentinel_backend;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import sentinel_backend.auth.Analyst;
import sentinel_backend.auth.AnalystRepository;
import sentinel_backend.auth.AnalystRole;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public final class TestUserFactory {

    private TestUserFactory() {
    }

    public static RequestPostProcessor testUser(
            AnalystRepository analystRepository,
            PasswordEncoder passwordEncoder,
            String username,
            AnalystRole role) {
        Analyst analyst = analystRepository
                .findByUsername(username)
                .orElseGet(() -> new Analyst(
                        username,
                        passwordEncoder.encode(
                                "TestPassword123!")));

        analyst.setRole(role);
        analyst.setEnabled(true);

        analystRepository.save(analyst);

        return user(username)
                .roles(role.name());
    }
}