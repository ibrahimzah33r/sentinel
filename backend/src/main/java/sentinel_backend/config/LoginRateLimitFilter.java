package sentinel_backend.config;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginRateLimitFilter
        extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(
                clientIp,
                ignored -> createBucket());

        if (!bucket.tryConsume(1)) {
            response.setStatus(429);

            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"message\":\"Too many login attempts\"}");

            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(
            HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && "/api/auth/login".equals(
                        request.getRequestURI());
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_ATTEMPTS)
                .refillIntervally(
                        MAX_ATTEMPTS,
                        Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}