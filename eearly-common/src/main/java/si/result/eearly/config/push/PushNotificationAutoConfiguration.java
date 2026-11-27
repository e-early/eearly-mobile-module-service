package si.result.eearly.config.push;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import si.result.eearly.service.push.LoggingPushNotificationService;
import si.result.eearly.service.push.PushNotificationService;

/**
 * Registers the logging default for {@link PushNotificationService}. Listed in
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports, so it is excluded
 * from component scanning and only applies when no other PushNotificationService bean exists.
 */
@AutoConfiguration
public class PushNotificationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(PushNotificationService.class)
  public PushNotificationService loggingPushNotificationService() {
    return new LoggingPushNotificationService();
  }
}
