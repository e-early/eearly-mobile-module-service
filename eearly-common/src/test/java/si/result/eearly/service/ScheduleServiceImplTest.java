package si.result.eearly.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.repository.ScheduleRepository;
import si.result.eearly.service.push.PushNotificationService;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

  private static final UUID SCHEDULE_ID = UUID.randomUUID();

  @Mock private ScheduleRepository scheduleRepository;
  @Mock private PushNotificationService pushNotificationService;

  private ScheduleServiceImpl scheduleService;
  private Schedule schedule;

  @BeforeEach
  void setUp() {
    scheduleService = new ScheduleServiceImpl(scheduleRepository, pushNotificationService);
    schedule = Schedule.createSchedule(UUID.randomUUID(), SCHEDULE_ID.toString(), "Name", "Description",
        true, UUID.randomUUID());
  }

  @Test
  void notifyAboutNewSchedules_delegatesToThePushPortWithTheLoadedSchedule() {
    when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

    scheduleService.notifyAboutNewSchedules(SCHEDULE_ID);

    verify(pushNotificationService).sendNewSchedule(schedule);
  }

  @Test
  void sendMeasurementReminder_delegatesToThePushPortWithTheLoadedSchedule() {
    when(scheduleRepository.findById(SCHEDULE_ID)).thenReturn(Optional.of(schedule));

    scheduleService.sendMeasurementReminder(SCHEDULE_ID, "2026-11-27T08:00:00Z", "Morning check");

    verify(pushNotificationService).sendMeasurementReminder(schedule, "2026-11-27T08:00:00Z", "Morning check");
  }

  @Test
  void notifyAboutNewSchedules_unknownScheduleThrowsAndSendsNothing() {
    when(scheduleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> scheduleService.notifyAboutNewSchedules(SCHEDULE_ID))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(pushNotificationService);
  }

  @Test
  void sendMeasurementReminder_unknownScheduleThrowsAndSendsNothing() {
    when(scheduleRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> scheduleService.sendMeasurementReminder(SCHEDULE_ID, "08:00", "Name"))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(pushNotificationService);
  }
}
