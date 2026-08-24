package si.result.eearly;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan({"si.result.eearly"})
@EnableJpaRepositories(basePackages = "si.result.eearly")
@EnableAutoConfiguration
public class DataTestConfig {
}
