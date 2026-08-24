package si.result.eearly.service;

import org.springframework.data.jpa.domain.Specification;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.schedule.CreateUpdateScheduleEntryCommand;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.domain.schedule.UpsertScheduleCommand;

import java.util.List;

public interface BackofficeScheduleService {

  List<Schedule> getSchedulesForCaretaker(Specification<Schedule> specification);

  Schedule createUpdateSchedule(String caretakerId, String id, String name, String description, boolean active) throws ValidationException;

  List<ScheduleEntry> createUpdateScheduleEntry(CreateUpdateScheduleEntryCommand command) throws ValidationException;

  Schedule addUserToSchedule(String userId, String scheduleId) throws ValidationException;

  void removeUserFromSchedule(String userId, String scheduleId);

  Schedule deleteScheduleEntry(String scheduleId, String scheduleEntryId) throws ValidationException;

  Schedule upsertSchedule(UpsertScheduleCommand command) throws ValidationException;

  void deleteSchedule(String scheduleId, String keycloakId) throws ValidationException;
}
