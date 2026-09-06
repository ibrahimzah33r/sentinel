package sentinel_backend.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sentinel_backend.auth.Analyst;
import sentinel_backend.auth.AnalystRepository;

@Component
public class AuthenticatedAnalystFilter
        extends OncePerRequestFilter {

    private final AnalystRepository analystRepository;

    public AuthenticatedAnalystFilter(
            AnalystRepository analystRepository) {
        this.analystRepository = analystRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                        authentication.getPrincipal())) {

            filterChain.doFilter(request, response);
            return;
        }

        Analyst analyst = analystRepository
                .findByUsername(authentication.getName())
                .orElse(null);

        if (analyst == null || !analyst.isEnabled()) {
            SecurityContextHolder.clearContext();

            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        String authority = "ROLE_" + analyst.getRole().name();

        boolean authorityChanged = authentication
                .getAuthorities()
                .stream()
                .noneMatch(existingAuthority -> existingAuthority
                        .getAuthority()
                        .equals(authority));

        if (authorityChanged
                || authentication.getAuthorities().size() != 1) {

            UsernamePasswordAuthenticationToken refreshedAuthentication = new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    authentication.getCredentials(),
                    List.of(
                            new SimpleGrantedAuthority(
                                    authority)));

            refreshedAuthentication.setDetails(
                    authentication.getDetails());

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            refreshedAuthentication);
        }

        filterChain.doFilter(request, response);
    }
}

