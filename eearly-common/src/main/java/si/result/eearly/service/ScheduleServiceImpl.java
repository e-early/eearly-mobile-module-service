package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.repository.ScheduleRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
  private final ScheduleRepository scheduleRepository;

  @Override
  public List<Schedule> getScheduleList(Specification<Schedule> specification) throws ValidationException {
    try {
      return scheduleRepository.findAll(specification);
    } catch (InvalidDataAccessApiUsageException e) {
      log.error("Invalid filter provided", e);
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, "The provided filter is invalid");
    }
  }

  @Override
  public void notifyAboutNewSchedules(UUID scheduleId) {
    Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> {
      log.error("Cant find schedule");
      return new IllegalArgumentException("Cant find schedule");
    });

    // Push notifications (Firebase) are not part of this OSS build -- no-op.
    log.debug("New schedule {} created but push notifications are not configured in this build", schedule.getId());
  }

  @Override
  public void sendMeasurementReminder(UUID scheduleId, String scheduledAt, String scheduleName) {
    Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> {
      log.error("Cannot find schedule {} for measurement reminder", scheduleId);
      return new IllegalArgumentException("Cannot find schedule: " + scheduleId);
    });

    // Push notifications (Firebase) are not part of this OSS build -- no-op.
    log.debug("Measurement reminder for schedule {} due but push notifications are not configured in this build", schedule.getId());
  }
}
