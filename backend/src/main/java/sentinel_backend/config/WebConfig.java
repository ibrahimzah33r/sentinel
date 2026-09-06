package sentinel_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Value("${sentinel.frontend.origin:http://localhost:5173}")
        private String frontendOrigin;

       @Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins(frontendOrigin)
            .allowedMethods(
                    "GET",
                    "POST",
                    "PUT",
                    "PATCH",
                    "DELETE",
                    "OPTIONS"
            )
            .allowedHeaders(
                    "Content-Type",
                    "X-CSRF-TOKEN"
            )
            .allowCredentials(true);
}
}