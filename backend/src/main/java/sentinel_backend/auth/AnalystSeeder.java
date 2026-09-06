package sentinel_backend.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AnalystSeeder implements CommandLineRunner {

    private final AnalystRepository analystRepository;
    private final PasswordEncoder passwordEncoder;

    public AnalystSeeder(
            AnalystRepository analystRepository,
            PasswordEncoder passwordEncoder) {
        this.analystRepository = analystRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String username = System.getenv("SENTINEL_ANALYST_USERNAME");
        String password = System.getenv("SENTINEL_ANALYST_PASSWORD");

        if (username == null || password == null) {
            return;
        }

        if (analystRepository.findByUsername(username).isPresent()) {
            return;
        }

        Analyst analyst = new Analyst(
                username,
                passwordEncoder.encode(password));
        analyst.setRole(AnalystRole.ADMIN);
        analystRepository.save(analyst);
    }

}