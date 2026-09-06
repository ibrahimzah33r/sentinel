package sentinel_backend.auth;

public record LoginRequest(
        String username,
        String password) {
}