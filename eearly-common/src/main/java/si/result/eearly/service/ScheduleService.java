package si.result.eearly.service;

import org.springframework.data.jpa.domain.Specification;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.schedule.Schedule;

import java.util.List;
import java.util.UUID;

public interface ScheduleService {
  List<Schedule> getScheduleList(Specification<Schedule> specification) throws ValidationException;

  void notifyAboutNewSchedules(UUID scheduleId);

  void sendMeasurementReminder(UUID scheduleId, String scheduledAt, String scheduleName);

}
