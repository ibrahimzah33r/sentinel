package sentinel_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

        private final AuthenticatedAnalystFilter authenticatedAnalystFilter;

        public SecurityConfig(AuthenticatedAnalystFilter authenticatedAnalystFilter) {
                this.authenticatedAnalystFilter = authenticatedAnalystFilter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(
                        HttpSecurity http)
                        throws Exception {

                return http
                                .cors(cors -> {
                                })
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(
                                                                (request, response, exception) -> response.sendError(
                                                                                HttpServletResponse.SC_UNAUTHORIZED)))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/login",
                                                                "/api/auth/csrf")
                                                .permitAll()
                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/**")
                                                .authenticated()
                                                .anyRequest()
                                                .permitAll())
                                .addFilterBefore(
                                                authenticatedAnalystFilter,
                                                AuthorizationFilter.class)
                                .build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}