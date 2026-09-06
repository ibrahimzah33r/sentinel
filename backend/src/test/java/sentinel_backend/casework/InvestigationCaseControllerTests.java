package sentinel_backend.casework;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sentinel_backend.TestUserFactory.testUser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.TestContainersConfig;
import sentinel_backend.auth.AnalystRepository;
import sentinel_backend.auth.AnalystRole;
import sentinel_backend.event.EventType;
import sentinel_backend.event.SecurityEventRequest;
import sentinel_backend.event.SecurityEventResponse;
import sentinel_backend.event.Severity;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class InvestigationCaseControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private JsonMapper jsonMapper;

        @Autowired
        private AnalystRepository analystRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private SecurityEventResponse createEvent(
                        String source,
                        EventType eventType,
                        Severity severity,
                        String message) throws Exception {

                SecurityEventRequest request = new SecurityEventRequest(
                                source,
                                eventType,
                                severity,
                                message,
                                null);

                String response = mockMvc.perform(
                                post("/api/events")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(
                                                                jsonMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                return jsonMapper.readValue(
                                response,
                                SecurityEventResponse.class);
        }

        private SecurityEventResponse escalateEvent(
                        long eventId) throws Exception {

                String response = mockMvc.perform(
                                patch(
                                                "/api/events/"
                                                                + eventId
                                                                + "/status")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .param("status", "ESCALATED"))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                return jsonMapper.readValue(
                                response,
                                SecurityEventResponse.class);
        }

        private InvestigationCaseResponse createCase(
                        long eventId) throws Exception {

                String response = mockMvc.perform(
                                post(
                                                "/api/cases/from-event/"
                                                                + eventId)
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                return jsonMapper.readValue(
                                response,
                                InvestigationCaseResponse.class);
        }

        @Test
        void shouldCreateCaseFromEscalatedEvent()
                        throws Exception {

                SecurityEventResponse event = createEvent(
                                "case-test",
                                EventType.FAILED_LOGIN,
                                Severity.HIGH,
                                "Repeated failed login attempts");

                escalateEvent(event.id());

                mockMvc.perform(
                                post(
                                                "/api/cases/from-event/"
                                                                + event.id())
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value("OPEN"))
                                .andExpect(
                                                jsonPath("$.eventId")
                                                                .value(event.id()))
                                .andExpect(
                                                jsonPath("$.eventType")
                                                                .value("FAILED_LOGIN"));
        }

        @Test
        void shouldRejectNonEscalatedEvent()
                        throws Exception {

                SecurityEventResponse event = createEvent(
                                "non-escalated-case-test",
                                EventType.PORT_SCAN,
                                Severity.MEDIUM,
                                "Port scan detected");

                mockMvc.perform(
                                post(
                                                "/api/cases/from-event/"
                                                                + event.id())
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldRejectDuplicateCase()
                        throws Exception {

                SecurityEventResponse event = createEvent(
                                "duplicate-case-test",
                                EventType.FAILED_LOGIN,
                                Severity.HIGH,
                                "Repeated failed login attempts");

                escalateEvent(event.id());
                createCase(event.id());

                mockMvc.perform(
                                post(
                                                "/api/cases/from-event/"
                                                                + event.id())
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isConflict());
        }

        @Test
        void shouldReturnNotFoundForMissingEvent()
                        throws Exception {

                mockMvc.perform(
                                post("/api/cases/from-event/999999999")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnCases()
                        throws Exception {

                mockMvc.perform(
                                get("/api/cases")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void shouldCloseCase()
                        throws Exception {

                SecurityEventResponse event = createEvent(
                                "close-case-test",
                                EventType.MALWARE_DETECTED,
                                Severity.CRITICAL,
                                "Malware detected");

                escalateEvent(event.id());

                InvestigationCaseResponse investigationCase = createCase(event.id());

                mockMvc.perform(
                                patch(
                                                "/api/cases/"
                                                                + investigationCase.id()
                                                                + "/status")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .param("status", "CLOSED"))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value("CLOSED"));
        }

        @Test
        void shouldReopenCase()
                        throws Exception {

                SecurityEventResponse event = createEvent(
                                "reopen-case-test",
                                EventType.MALWARE_DETECTED,
                                Severity.CRITICAL,
                                "Malware detected");

                escalateEvent(event.id());

                InvestigationCaseResponse investigationCase = createCase(event.id());

                mockMvc.perform(
                                patch(
                                                "/api/cases/"
                                                                + investigationCase.id()
                                                                + "/status")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .param("status", "CLOSED"))
                                .andExpect(status().isOk());

                mockMvc.perform(
                                patch(
                                                "/api/cases/"
                                                                + investigationCase.id()
                                                                + "/status")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .param("status", "OPEN"))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value("OPEN"));
        }
}