package si.result.eearly.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Infrastructure switches the core needs in every application, kept in the library so an
 * application class only has to carry {@code @SpringBootApplication}. Picked up by component scan
 * of {@code si.result.eearly}; an application outside that package imports it.
 * (Caching is enabled by {@code CacheConfig}, JPA by the application's own scan.)
 */
@Configuration
@EnableScheduling
public class CoreApplicationConfiguration {

  @Configuration
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  @EnableWebMvc
  static class WebMvcConfiguration {}
}
