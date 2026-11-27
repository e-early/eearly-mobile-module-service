package si.result.eearly.service.push;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import si.result.eearly.domain.schedule.Schedule;

class LoggingPushNotificationServiceTest {

  private static final String TOKEN = "secret-device-token-123";
  private static final String TITLE = "Secret title";
  private static final String BODY = "Secret body text";
  private static final String SCHEDULE_NAME = "Secret schedule name";

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;
  private Level previousLevel;

  @BeforeEach
  void attachAppender() {
    logger = (Logger) LoggerFactory.getLogger(LoggingPushNotificationService.class);
    previousLevel = logger.getLevel();
    logger.setLevel(Level.DEBUG);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void detachAppender() {
    logger.detachAppender(appender);
    logger.setLevel(previousLevel);
  }

  @Test
  void constructionLogsOneInfoLine() {
    new LoggingPushNotificationService();

    List<ILoggingEvent> infoEvents = events().stream()
        .filter(e -> e.getLevel() == Level.INFO)
        .toList();
    assertThat(infoEvents).hasSize(1);
    assertThat(infoEvents.get(0).getFormattedMessage())
        .isEqualTo("No push notification provider configured; push notifications are logged, not sent");
  }

  @Test
  void sendNotificationForTokenSendsNothingAndReturnsEmpty() {
    LoggingPushNotificationService service = new LoggingPushNotificationService();

    assertThat(service.sendNotificationForToken(TOKEN, TITLE, BODY)).isEmpty();
  }

  @Test
  void noCallLogsTheTokenTitleBodyOrScheduleName() {
    LoggingPushNotificationService service = new LoggingPushNotificationService();
    Schedule schedule = Schedule.createSchedule(UUID.randomUUID(), UUID.randomUUID().toString(),
        SCHEDULE_NAME, "Secret description", true, UUID.randomUUID());

    service.sendNotificationForToken(TOKEN, TITLE, BODY);
    service.sendNewSchedule(schedule);
    service.sendMeasurementReminder(schedule, "2026-11-27T08:00:00Z", SCHEDULE_NAME);

    String logged = events().stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.joining("\n"));
    assertThat(logged)
        .contains(schedule.getId().toString())
        .doesNotContain(TOKEN)
        .doesNotContain(TITLE)
        .doesNotContain(BODY)
        .doesNotContain(SCHEDULE_NAME)
        .doesNotContain("Secret description");
  }

  private List<ILoggingEvent> events() {
    return appender.list;
  }
}
