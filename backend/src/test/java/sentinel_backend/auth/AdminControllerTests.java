package sentinel_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sentinel_backend.TestUserFactory.testUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import sentinel_backend.TestContainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class AdminControllerTests {

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
        void anonymousUserCannotCreateAnalyst()
                        throws Exception {

                mockMvc.perform(
                                post("/api/admin/analysts")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "username": "analyst2",
                                                                  "password": "StrongPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void normalAnalystCannotCreateAnalyst()
                        throws Exception {

                mockMvc.perform(
                                post("/api/admin/analysts")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "username": "analyst2",
                                                                  "password": "StrongPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminCanCreateAnalyst()
                        throws Exception {

                mockMvc.perform(
                                post("/api/admin/analysts")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "admin",
                                                                                AnalystRole.ADMIN))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "username": "analyst2",
                                                                  "password": "StrongPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.username")
                                                                .value("analyst2"))
                                .andExpect(
                                                jsonPath("$.role")
                                                                .value("ANALYST"))
                                .andExpect(
                                                jsonPath("$.enabled")
                                                                .value(true));

                Analyst analyst = analystRepository
                                .findByUsername("analyst2")
                                .orElseThrow();

                assertThat(analyst.getRole())
                                .isEqualTo(AnalystRole.ANALYST);

                assertThat(
                                passwordEncoder.matches(
                                                "StrongPassword123!",
                                                analyst.getPasswordHash()))
                                .isTrue();
        }

        @Test
        void normalAnalystCannotResetPassword()
                        throws Exception {

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("OldPassword123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId()
                                                                + "/password")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "password": "NewPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminCanResetPassword()
                        throws Exception {

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("OldPassword123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId()
                                                                + "/password")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "admin",
                                                                                AnalystRole.ADMIN))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "password": "NewPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isOk());

                Analyst updatedAnalyst = analystRepository
                                .findById(savedAnalyst.getId())
                                .orElseThrow();

                assertThat(
                                passwordEncoder.matches(
                                                "NewPassword123!",
                                                updatedAnalyst.getPasswordHash()))
                                .isTrue();
        }

        @Test
        void adminCanDisableAnalyst()
                        throws Exception {

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("Password123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId()
                                                                + "/enabled")
                                                .param("enabled", "false")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "admin",
                                                                                AnalystRole.ADMIN))
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.enabled")
                                                                .value(false));

                Analyst updatedAnalyst = analystRepository
                                .findById(savedAnalyst.getId())
                                .orElseThrow();

                assertThat(updatedAnalyst.isEnabled())
                                .isFalse();
        }

        @Test
        void disabledAnalystCannotLogin()
                        throws Exception {

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("Password123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(false);

                analystRepository.save(analyst);

                mockMvc.perform(
                                post("/api/auth/login")
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "username": "analyst2",
                                                                  "password": "Password123!"
                                                                }
                                                                """))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void resettingMissingAnalystReturnsNotFound()
                        throws Exception {

                mockMvc.perform(
                                patch("/api/admin/analysts/99999/password")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "admin",
                                                                                AnalystRole.ADMIN))
                                                .with(csrf())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                  "password": "NewPassword123!"
                                                                }
                                                                """))
                                .andExpect(status().isNotFound());
        }

        @Test
        void adminCanDeleteAnalyst()
                        throws Exception {

                Analyst admin = new Analyst(
                                "admin",
                                passwordEncoder.encode("Password123!"));
                admin.setRole(AnalystRole.ADMIN);
                admin.setEnabled(true);

                Analyst savedAdmin = analystRepository.save(admin);

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("Password123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                mockMvc.perform(
                                delete(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId())
                                                .with(
                                                                user(
                                                                                savedAdmin.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isNoContent());

                assertThat(
                                analystRepository.existsById(
                                                savedAnalyst.getId()))
                                .isFalse();
        }

        @Test
        void adminCanDeleteAdminWhenAnotherEnabledAdminExists()
                        throws Exception {

                Analyst adminOne = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                adminOne.setRole(AnalystRole.ADMIN);
                adminOne.setEnabled(true);

                Analyst savedAdminOne = analystRepository.save(adminOne);

                Analyst adminTwo = new Analyst(
                                "admin2",
                                passwordEncoder.encode("Password123!"));
                adminTwo.setRole(AnalystRole.ADMIN);
                adminTwo.setEnabled(true);

                Analyst savedAdminTwo = analystRepository.save(adminTwo);

                mockMvc.perform(
                                delete(
                                                "/api/admin/analysts/"
                                                                + savedAdminTwo.getId())
                                                .with(
                                                                user(
                                                                                savedAdminOne.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isNoContent());

                assertThat(
                                analystRepository.existsById(
                                                savedAdminTwo.getId()))
                                .isFalse();

                assertThat(
                                analystRepository.existsById(
                                                savedAdminOne.getId()))
                                .isTrue();
        }

        @Test
        void adminCannotDeleteLastEnabledAdmin()
                        throws Exception {

                Analyst admin = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                admin.setRole(AnalystRole.ADMIN);
                admin.setEnabled(true);

                Analyst savedAdmin = analystRepository.save(admin);

                mockMvc.perform(
                                delete(
                                                "/api/admin/analysts/"
                                                                + savedAdmin.getId())
                                                .with(
                                                                user(
                                                                                savedAdmin.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                assertThat(
                                analystRepository.existsById(
                                                savedAdmin.getId()))
                                .isTrue();
        }

        @Test
        void adminCannotDisableLastEnabledAdmin()
                        throws Exception {

                Analyst admin = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                admin.setRole(AnalystRole.ADMIN);
                admin.setEnabled(true);

                Analyst savedAdmin = analystRepository.save(admin);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAdmin.getId()
                                                                + "/enabled")
                                                .param("enabled", "false")
                                                .with(
                                                                user(
                                                                                savedAdmin.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                Analyst unchangedAdmin = analystRepository
                                .findById(savedAdmin.getId())
                                .orElseThrow();

                assertThat(unchangedAdmin.isEnabled())
                                .isTrue();
        }

        @Test
        void adminCanPromoteAnalystToAdmin()
                        throws Exception {

                Analyst analyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("Password123!"));
                analyst.setRole(AnalystRole.ANALYST);
                analyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(analyst);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId()
                                                                + "/role")
                                                .param("role", "ADMIN")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "admin",
                                                                                AnalystRole.ADMIN))
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.role")
                                                                .value("ADMIN"));

                Analyst updatedAnalyst = analystRepository
                                .findById(savedAnalyst.getId())
                                .orElseThrow();

                assertThat(updatedAnalyst.getRole())
                                .isEqualTo(AnalystRole.ADMIN);
        }

        @Test
        void adminCanDemoteAdminWhenAnotherEnabledAdminExists()
                        throws Exception {

                Analyst adminOne = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                adminOne.setRole(AnalystRole.ADMIN);
                adminOne.setEnabled(true);

                Analyst savedAdminOne = analystRepository.save(adminOne);

                Analyst adminTwo = new Analyst(
                                "admin2",
                                passwordEncoder.encode("Password123!"));
                adminTwo.setRole(AnalystRole.ADMIN);
                adminTwo.setEnabled(true);

                Analyst savedAdminTwo = analystRepository.save(adminTwo);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAdminTwo.getId()
                                                                + "/role")
                                                .param("role", "ANALYST")
                                                .with(
                                                                user(
                                                                                savedAdminOne.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(
                                                jsonPath("$.role")
                                                                .value("ANALYST"));
        }

        @Test
        void adminCannotDemoteLastEnabledAdmin()
                        throws Exception {

                Analyst admin = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                admin.setRole(AnalystRole.ADMIN);
                admin.setEnabled(true);

                Analyst savedAdmin = analystRepository.save(admin);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAdmin.getId()
                                                                + "/role")
                                                .param("role", "ANALYST")
                                                .with(
                                                                user(
                                                                                savedAdmin.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf()))
                                .andExpect(status().isBadRequest());

                Analyst unchangedAdmin = analystRepository
                                .findById(savedAdmin.getId())
                                .orElseThrow();

                assertThat(unchangedAdmin.getRole())
                                .isEqualTo(AnalystRole.ADMIN);
        }

        @Test
        void analystCannotChangeRole()
                        throws Exception {

                Analyst targetAnalyst = new Analyst(
                                "analyst2",
                                passwordEncoder.encode("Password123!"));
                targetAnalyst.setRole(AnalystRole.ANALYST);
                targetAnalyst.setEnabled(true);

                Analyst savedAnalyst = analystRepository.save(targetAnalyst);

                mockMvc.perform(
                                patch(
                                                "/api/admin/analysts/"
                                                                + savedAnalyst.getId()
                                                                + "/role")
                                                .param("role", "ADMIN")
                                                .with(
                                                                testUser(
                                                                                analystRepository,
                                                                                passwordEncoder,
                                                                                "analyst",
                                                                                AnalystRole.ANALYST))
                                                .with(csrf()))
                                .andExpect(status().isForbidden());
        }

        @Test
        void adminDeletingOwnAccountInvalidatesSession()
                        throws Exception {

                Analyst adminOne = new Analyst(
                                "admin1",
                                passwordEncoder.encode("Password123!"));
                adminOne.setRole(AnalystRole.ADMIN);
                adminOne.setEnabled(true);

                Analyst savedAdminOne = analystRepository.save(adminOne);

                Analyst adminTwo = new Analyst(
                                "admin2",
                                passwordEncoder.encode("Password123!"));
                adminTwo.setRole(AnalystRole.ADMIN);
                adminTwo.setEnabled(true);

                analystRepository.save(adminTwo);

                MockHttpSession session = new MockHttpSession();

                mockMvc.perform(
                                delete(
                                                "/api/admin/analysts/"
                                                                + savedAdminOne.getId())
                                                .with(
                                                                user(
                                                                                savedAdminOne.getUsername())
                                                                                .roles("ADMIN"))
                                                .with(csrf())
                                                .session(session))
                                .andExpect(status().isNoContent());

                assertThat(
                                analystRepository.existsById(
                                                savedAdminOne.getId()))
                                .isFalse();

                assertThat(session.isInvalid())
                                .isTrue();
        }
}