package sentinel_backend.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sentinel_backend.TestUserFactory.testUser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.TestContainersConfig;
import sentinel_backend.auth.AnalystRepository;
import sentinel_backend.auth.AnalystRole;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class SecurityApiTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AnalystRepository analystRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void eventsRequireAuthentication()
                        throws Exception {

                mockMvc.perform(
                                get("/api/events"))
                                .andExpect(
                                                status()
                                                                .isUnauthorized());
        }

        @Test
        void eventStatusUpdateWithoutCsrfIsForbidden()
                        throws Exception {

                mockMvc.perform(
                                patch("/api/events/99999/status")
                                                .param(
                                                                "status",
                                                                "REVIEWED")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST)))
                                .andExpect(
                                                status()
                                                                .isForbidden());
        }

        @Test
        void missingEventReturnsNotFound()
                        throws Exception {

                mockMvc.perform(
                                patch("/api/events/99999/status")
                                                .param(
                                                                "status",
                                                                "REVIEWED")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(
                                                                csrf()
                                                                                .asHeader()))
                                .andExpect(
                                                status()
                                                                .isNotFound());
        }
}