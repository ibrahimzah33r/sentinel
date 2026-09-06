package sentinel_backend.casework;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/cases")
public class InvestigationCaseController {

    private final InvestigationCaseService service;

    public InvestigationCaseController(
            InvestigationCaseService service) {
        this.service = service;
    }

    @PostMapping("/from-event/{eventId}")
    public ResponseEntity<InvestigationCaseResponse> createFromEvent(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(
                service.createFromEvent(eventId));
    }

    @GetMapping
    public ResponseEntity<List<InvestigationCaseResponse>> getAll() {
        return ResponseEntity.ok(
                service.getAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvestigationCaseResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam CaseStatus status) {
        return ResponseEntity.ok(
                service.updateStatus(id, status));
    }

}