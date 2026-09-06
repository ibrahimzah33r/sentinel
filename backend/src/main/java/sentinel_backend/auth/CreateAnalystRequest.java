package sentinel_backend.auth;

public record CreateAnalystRequest(
        String username,
        String password) {
}