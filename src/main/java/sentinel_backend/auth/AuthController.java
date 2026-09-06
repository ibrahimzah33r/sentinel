package sentinel_backend.auth;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {
                LoginResponse response;

                try {
                        response = authService.login(request);
                } catch (IllegalArgumentException exception) {
                        return ResponseEntity.status(401).build();
                }

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                "ROLE_" + response.role().name());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                response.username(),
                                null,
                                List.of(authority));

                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                securityContext.setAuthentication(authentication);

                HttpSession session = httpRequest.getSession(true);

                httpRequest.changeSessionId();

                session.setAttribute(
                                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                                securityContext);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<Void> logout(
                        HttpServletRequest request) {
                HttpSession session = request.getSession(false);

                if (session != null) {
                        session.invalidate();
                }

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/csrf")
        public ResponseEntity<CsrfTokenResponse> csrf(CsrfToken csrfToken) {
                return ResponseEntity.ok(
                                new CsrfTokenResponse(csrfToken.getToken()));
        }

        @GetMapping("/me")
        public ResponseEntity<LoginResponse> me(
                        Authentication authentication) {
                return ResponseEntity.ok(
                                authService.getCurrentUser(
                                                authentication.getName()));
        }
}