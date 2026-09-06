package sentinel_backend.auth;

public record LoginResponse(
                Long id,
                String username,
                AnalystRole role) {
}