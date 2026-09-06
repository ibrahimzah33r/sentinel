package sentinel_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestContainersConfig.class)
class SentinelBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
