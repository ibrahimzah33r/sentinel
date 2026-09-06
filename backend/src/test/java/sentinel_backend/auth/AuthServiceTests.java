package sentinel_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import sentinel_backend.error.InvalidOperationException;
import sentinel_backend.error.ResourceConflictException;
import sentinel_backend.error.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private AnalystRepository analystRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                analystRepository,
                passwordEncoder);
    }

    @Test
    void loginReturnsAnalystWhenCredentialsAreValid() {
        Analyst analyst = new Analyst(
                "bob",
                "hashed-password");

        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(true);

        when(
                analystRepository.findByUsername("bob")).thenReturn(
                        Optional.of(analyst));

        when(
                passwordEncoder.matches(
                        "password",
                        "hashed-password"))
                .thenReturn(true);

        LoginResponse response = authService.login(
                new LoginRequest(
                        "bob",
                        "password"));

        assertThat(response.username())
                .isEqualTo("bob");

        assertThat(response.role())
                .isEqualTo(AnalystRole.ANALYST);
    }

    @Test
    void disabledAnalystCannotLogin() {
        Analyst analyst = new Analyst(
                "bob",
                "hashed-password");

        analyst.setRole(AnalystRole.ANALYST);
        analyst.setEnabled(false);

        when(
                analystRepository.findByUsername("bob")).thenReturn(
                        Optional.of(analyst));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(
                        "bob",
                        "password")))
                .isInstanceOf(
                        IllegalArgumentException.class);
    }

    @Test
    void createAnalystHashesPasswordAndUsesAnalystRole() {
        CreateAnalystRequest request = new CreateAnalystRequest(
                "bob",
                "password");

        when(
                analystRepository.findByUsername("bob")).thenReturn(
                        Optional.empty());

        when(
                passwordEncoder.encode("password")).thenReturn("hashed-password");

        when(
                analystRepository.save(
                        org.mockito.ArgumentMatchers
                                .any(Analyst.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalystResponse response = authService.createAnalyst(request);

        assertThat(response.username())
                .isEqualTo("bob");

        assertThat(response.role())
                .isEqualTo(AnalystRole.ANALYST);

        assertThat(response.enabled())
                .isTrue();

        verify(passwordEncoder)
                .encode("password");
    }

    @Test
    void duplicateUsernameCannotBeCreated() {
        Analyst existingAnalyst = new Analyst(
                "bob",
                "hashed-password");

        when(
                analystRepository.findByUsername("bob")).thenReturn(
                        Optional.of(existingAnalyst));

        assertThatThrownBy(() -> authService.createAnalyst(
                new CreateAnalystRequest(
                        "bob",
                        "password")))
                .isInstanceOf(
                        ResourceConflictException.class);
    }

    @Test
    void adminAccountCannotBeDisabled() {
        Analyst admin = new Analyst(
                "admin",
                "hashed-password");

        admin.setRole(AnalystRole.ADMIN);
        admin.setEnabled(true);

        when(
                analystRepository.findById(1L)).thenReturn(
                        Optional.of(admin));

        assertThatThrownBy(() -> authService.setAnalystEnabled(
                1L,
                false))
                .isInstanceOf(
                        InvalidOperationException.class);
    }

    @Test
    void missingAnalystCannotHavePasswordReset() {
        when(
                analystRepository.findById(99L)).thenReturn(
                        Optional.empty());

        assertThatThrownBy(() -> authService.resetAnalystPassword(
                99L,
                new ResetAnalystPasswordRequest(
                        "password")))
                .isInstanceOf(
                        ResourceNotFoundException.class);
    }
}