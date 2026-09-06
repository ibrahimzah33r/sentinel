package sentinel_backend.auth;

public record AnalystResponse(
        Long id,
        String username,
        AnalystRole role,
        boolean enabled) {
}