package si.result.eearly.config;

import java.io.IOException;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Registers {@code eearly-mobile-defaults.yml} as the lowest-precedence property source, so every
 * application built on the core libraries starts with the same defaults while its own
 * {@code application.yml}, profile files, environment variables and system properties still win.
 */
public class CoreDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  public static final String PROPERTY_SOURCE_NAME = "eearly-mobile-defaults";
  static final String RESOURCE = "eearly-mobile-defaults.yml";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    Resource resource = new ClassPathResource(RESOURCE);
    if (!resource.exists() || environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
      return;
    }
    List<PropertySource<?>> documents;
    try {
      documents = new YamlPropertySourceLoader().load(PROPERTY_SOURCE_NAME, resource);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot load " + RESOURCE, e);
    }
    // YAML documents are in order: a later document overrides an earlier one.
    CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
    for (PropertySource<?> document : documents) {
      composite.addFirstPropertySource(document);
    }
    environment.getPropertySources().addLast(composite);
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
