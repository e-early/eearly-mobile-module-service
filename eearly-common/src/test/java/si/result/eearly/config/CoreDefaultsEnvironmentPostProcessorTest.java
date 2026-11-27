package si.result.eearly.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class CoreDefaultsEnvironmentPostProcessorTest {

  @Test
  void higherPrecedenceSourceOverridesDefaultAndDefaultOnlyKeysArePresent() {
    MockEnvironment env = new MockEnvironment();
    env.getPropertySources().addFirst(new MapPropertySource("app",
        Map.of("onboarding.rate-limit.requests-per-minute", "99")));

    new CoreDefaultsEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());

    assertThat(env.getProperty("onboarding.rate-limit.requests-per-minute")).isEqualTo("99");
    assertThat(env.getProperty("hashids.min-hash-length")).isEqualTo("6");
    assertThat(env.getProperty("server.port")).isEqualTo("8081");
  }

  @Test
  void defaultsAreTheLastPropertySource() {
    MockEnvironment env = new MockEnvironment();
    new CoreDefaultsEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());

    var sources = env.getPropertySources().stream().map(s -> s.getName()).toList();
    assertThat(sources.get(sources.size() - 1))
        .isEqualTo(CoreDefaultsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
  }

  @Test
  void applicationWithoutOwnConfigurationGetsTheDefaults() {
    try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyApp.class)
        .web(WebApplicationType.NONE).run()) {
      assertThat(ctx.getEnvironment().getProperty("onboarding.rate-limit.requests-per-minute"))
          .isEqualTo("10");
      assertThat(ctx.getEnvironment().getProperty("hashids.min-hash-length")).isEqualTo("6");
    }
  }

  @Test
  void applicationPropertiesOverrideTheDefaults() {
    try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(EmptyApp.class)
        .web(WebApplicationType.NONE)
        .run("--hashids.min-hash-length=9")) {
      assertThat(ctx.getEnvironment().getProperty("hashids.min-hash-length")).isEqualTo("9");
    }
  }

  @Configuration
  static class EmptyApp {}
}
