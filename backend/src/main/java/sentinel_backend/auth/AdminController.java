package sentinel_backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/admin/analysts")
public class AdminController {
        private final AuthService authService;

        public AdminController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping
        public ResponseEntity<AnalystResponse> createAnalyst(
                        @RequestBody CreateAnalystRequest request) {
                return ResponseEntity.ok(
                                authService.createAnalyst(request));
        }

        @PatchMapping("/{id}/password")
        public ResponseEntity<AnalystResponse> resetPassword(
                        @PathVariable Long id,
                        @RequestBody ResetAnalystPasswordRequest request) {
                return ResponseEntity.ok(
                                authService.resetAnalystPassword(
                                                id,
                                                request));
        }

        @PatchMapping("/{id}/enabled")
        public ResponseEntity<AnalystResponse> setEnabled(
                        @PathVariable Long id,
                        @RequestParam boolean enabled) {
                return ResponseEntity.ok(
                                authService.setAnalystEnabled(
                                                id,
                                                enabled));
        }

        @GetMapping
        public ResponseEntity<List<AnalystResponse>> getAnalysts() {
                return ResponseEntity.ok(
                                authService.getAnalysts());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAnalyst(
                        @PathVariable Long id,
                        Authentication authentication,
                        HttpServletRequest request) {
                LoginResponse currentUser = authService.getCurrentUser(
                                authentication.getName());

                boolean deletingSelf = currentUser.id().equals(id);

                authService.deleteAnalyst(id);

                if (deletingSelf) {
                        HttpSession session = request.getSession(false);

                        if (session != null) {
                                session.invalidate();
                        }
                }

                return ResponseEntity.noContent().build();
        }

        @PatchMapping("/{id}/role")
        public ResponseEntity<AnalystResponse> setAnalystRole(
                        @PathVariable Long id,
                        @RequestParam AnalystRole role) {
                return ResponseEntity.ok(
                                authService.setAnalystRole(id, role));
        }

}