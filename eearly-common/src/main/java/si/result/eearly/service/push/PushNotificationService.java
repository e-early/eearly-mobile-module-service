package si.result.eearly.service.push;

import si.result.eearly.domain.schedule.Schedule;

/**
 * Port for sending push notifications to the patient's app.
 *
 * <p>Implemented by an add-on module that talks to a push provider. This core build ships only
 * {@link LoggingPushNotificationService}, which sends nothing and logs each call. An add-on provides
 * its implementation as a Spring bean; it then replaces the default, which backs off via
 * {@code @ConditionalOnMissingBean}.
 */
public interface PushNotificationService {

  /**
   * Sends one notification to a single device token.
   *
   * @return the provider's message id, or an empty string when nothing was sent
   */
  String sendNotificationForToken(String token, String title, String body);

  /** Tells the patients of the schedule that a new schedule was created for them. */
  void sendNewSchedule(Schedule schedule);

  /** Reminds the patients of the schedule that a measurement is due. */
  void sendMeasurementReminder(Schedule schedule, String scheduledAt, String scheduleName);
}
