package si.result.eearly.service.push;

import lombok.extern.slf4j.Slf4j;
import si.result.eearly.domain.schedule.Schedule;

/**
 * Default {@link PushNotificationService} for builds without a push provider. It sends nothing.
 *
 * <p>Logging rules: tokens, titles, bodies and schedule names are personal or sensitive and are
 * never logged. Only the schedule id is logged.
 *
 * <p>Deliberately not a Spring component. It is registered by {@code PushNotificationAutoConfiguration}
 * so that an add-on's bean can replace it.
 */
@Slf4j
public class LoggingPushNotificationService implements PushNotificationService {

  public LoggingPushNotificationService() {
    log.info("No push notification provider configured; push notifications are logged, not sent");
  }

  @Override
  public String sendNotificationForToken(String token, String title, String body) {
    log.debug("Push notification for a device token not sent: no provider configured");
    return "";
  }

  @Override
  public void sendNewSchedule(Schedule schedule) {
    log.debug("New schedule notification for schedule {} not sent: no provider configured", schedule.getId());
  }

  @Override
  public void sendMeasurementReminder(Schedule schedule, String scheduledAt, String scheduleName) {
    log.debug("Measurement reminder for schedule {} not sent: no provider configured", schedule.getId());
  }
}
