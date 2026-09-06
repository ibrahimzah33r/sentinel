package sentinel_backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.TestContainersConfig;
import sentinel_backend.auth.Analyst;
import sentinel_backend.auth.AnalystRepository;
import sentinel_backend.auth.AnalystRole;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class AuthenticatedAnalystFilterTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalystRepository analystRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        analystRepository.deleteAll();
    }

    @Test
    void disabledAuthenticatedAnalystIsRejected()
            throws Exception {

        Analyst analyst = new Analyst(
                "analyst",
                passwordEncoder.encode("Password123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(false);

        analystRepository.save(analyst);

        mockMvc.perform(
                get("/api/events")
                        .with(
                                user("analyst")
                                        .roles("ANALYST")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletedAuthenticatedAnalystIsRejected()
            throws Exception {

        mockMvc.perform(
                get("/api/events")
                        .with(
                                user("deleted-user")
                                        .roles("ANALYST")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demotedAdminLosesAdminAccess()
            throws Exception {

        Analyst analyst = new Analyst(
                "admin",
                passwordEncoder.encode("Password123!"));
        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(true);

        analystRepository.save(analyst);

        mockMvc.perform(
                get("/api/admin/analysts")
                        .with(
                                user("admin")
                                        .roles("ADMIN")))
                .andExpect(status().isForbidden());
    }
}