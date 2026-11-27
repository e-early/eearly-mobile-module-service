package si.result.eearly.config.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import si.result.eearly.service.push.LoggingPushNotificationService;
import si.result.eearly.service.push.PushNotificationService;

class PushNotificationAutoConfigurationTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(PushNotificationAutoConfiguration.class));

  @Test
  void registersTheLoggingDefaultWhenNoProviderIsPresent() {
    runner.run(context -> {
      assertThat(context).hasSingleBean(PushNotificationService.class);
      assertThat(context.getBean(PushNotificationService.class))
          .isInstanceOf(LoggingPushNotificationService.class);
    });
  }

  @Test
  void aProviderBeanReplacesTheLoggingDefault() {
    PushNotificationService provider = mock(PushNotificationService.class);

    runner.withBean("pushProvider", PushNotificationService.class, () -> provider).run(context -> {
      assertThat(context).hasSingleBean(PushNotificationService.class);
      assertThat(context.getBean(PushNotificationService.class)).isSameAs(provider);
      assertThat(context).doesNotHaveBean(LoggingPushNotificationService.class);
    });
  }

  @Test
  void componentScanWithBootExclusionDoesNotRegisterTheAutoConfigurationAsAComponent() {
    try (var context = new AnnotationConfigApplicationContext(BootStyleScan.class)) {
      assertThat(context.getBeansOfType(PushNotificationAutoConfiguration.class)).isEmpty();
      assertThat(context.getBeansOfType(PushNotificationService.class)).isEmpty();
    }
  }

  @Configuration
  @ComponentScan(basePackageClasses = PushNotificationAutoConfiguration.class,
      excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
          classes = AutoConfigurationExcludeFilter.class))
  static class BootStyleScan {}
}
