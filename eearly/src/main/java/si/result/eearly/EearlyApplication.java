package si.result.eearly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@EnableCaching
@EnableScheduling
public class EearlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(EearlyApplication.class, args);
	}
}
