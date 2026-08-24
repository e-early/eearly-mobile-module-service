package si.result.eearly.service;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.caretaker.Caretaker;
import si.result.eearly.domain.enums.MeasurementMode;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
import si.result.eearly.domain.enums.ScheduledMode;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.schedule.*;
import si.result.eearly.repository.*;
import si.result.eearly.domain.measurement.MeasurementType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class BackofficeScheduleServiceImpl implements BackofficeScheduleService {
  private final BackofficeScheduleRepository backofficeScheduleRepository;
  private final ScheduleUserRepository scheduleUserRepository;
  private final CaretakerRepository caretakerRepository;
  private final ScheduleEntryRepository scheduleEntryRepository;
  private final MeasurementTypeRepository measurementTypeRepository;

  @Override
  public List<Schedule> getSchedulesForCaretaker(Specification<Schedule> specification) {
      return backofficeScheduleRepository.findAll(specification);
  }

  @Override
  public Schedule upsertSchedule(UpsertScheduleCommand command) throws ValidationException {
    Optional<Caretaker> caretakerOption = caretakerRepository.findById(UUID.fromString(command.caretakerId()));

    if (caretakerOption.isEmpty()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, String.format("Caretaker with id %s does not exist", command.caretakerId()));
    }

    Schedule schedule;
    if (!StringUtil.isNullOrEmpty(command.id())) {
      Optional<Schedule> scheduleOption = backofficeScheduleRepository.findById(UUID.fromString(command.id()));
      if (scheduleOption.isPresent()) {
        schedule = scheduleOption.get();
        schedule.setName(command.name());
        schedule.setDescription(command.description());
        schedule.setActive(command.active());
      } else {
        schedule = Schedule.createScheduleFromUpsertCommand(command, caretakerOption.get().getFacility().getId());
      }
    } else {
      schedule = Schedule.createScheduleFromUpsertCommand(command, caretakerOption.get().getFacility().getId());
    }

    if (schedule.getScheduleEntries() != null && !schedule.getScheduleEntries().isEmpty()) {
      List<ScheduleEntry> entriesToDelete = new java.util.ArrayList<>(schedule.getScheduleEntries());
      schedule.getScheduleEntries().clear();
      backofficeScheduleRepository.saveAndFlush(schedule);
      scheduleEntryRepository.deleteAll(entriesToDelete);
    } else if (schedule.getScheduleEntries() == null) {
      schedule.setScheduleEntries(new java.util.ArrayList<>());
    }

    if (command.measurements() != null && !command.measurements().isEmpty()) {
      List<ScheduleEntry> savedEntries = scheduleEntryRepository.saveAll(command.measurements());
      schedule.getScheduleEntries().addAll(savedEntries);
    }

    var savedSchedule = backofficeScheduleRepository.save(schedule);

    addUserToSchedule(command.profileId(), savedSchedule.getId().toString());

    return savedSchedule;
  }

  @Override
  public void deleteSchedule(String scheduleId, String keycloakId) throws ValidationException {
    UUID scheduleUuid = UUID.fromString(scheduleId);
    Optional<Schedule> scheduleOption = backofficeScheduleRepository.findById(scheduleUuid);

    if (scheduleOption.isEmpty()) {
      log.info("Schedule {} does not exist in mobile backend, treating delete as already complete", scheduleId);
      return;
    }

    Schedule schedule = scheduleOption.get();

    boolean userHasAccess = schedule.getProfiles() != null && 
        schedule.getProfiles().stream()
            .anyMatch(profile -> profile.getUserId().toString().equals(keycloakId));
    
    if (!userHasAccess) {
        throw new ValidationException(ErrorCode.VALIDATION_ERROR,
            String.format("Schedule %s is not assigned to user %s", scheduleId, keycloakId));
    }

    scheduleUserRepository.deleteByScheduleId(scheduleUuid);
    log.debug("Deleted schedule_user entries for schedule {}", scheduleId);

    if (schedule.getScheduleEntries() != null && !schedule.getScheduleEntries().isEmpty()) {
      List<ScheduleEntry> entriesToDelete = new ArrayList<>(schedule.getScheduleEntries());
      schedule.getScheduleEntries().clear();
      backofficeScheduleRepository.saveAndFlush(schedule);
      scheduleEntryRepository.deleteAll(entriesToDelete);
      log.debug("Deleted {} schedule entries for schedule {}", entriesToDelete.size(), scheduleId);
    }

    backofficeScheduleRepository.delete(schedule);
    log.info("Successfully deleted schedule {} and all related data", scheduleId);
  }

  @Override
  public Schedule createUpdateSchedule(String caretakerId, String id, String name, String description, boolean active) throws ValidationException {
    Optional<Caretaker> caretakerOption = caretakerRepository.findById(UUID.fromString(caretakerId));
    if (caretakerOption.isPresent()) {
      final var scheduleToSave = Schedule.createSchedule(UUID.fromString(caretakerId), id, name, description, active, caretakerOption.get().getFacility().getId());
      return backofficeScheduleRepository.save(scheduleToSave);
    } else {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, String.format("Caretaker with id %s does not exist", caretakerId));
    }
  }

  @Override
  public List<ScheduleEntry> createUpdateScheduleEntry(CreateUpdateScheduleEntryCommand command) throws ValidationException {
    Optional<Schedule> possibleSchedule = backofficeScheduleRepository.findById(UUID.fromString(command.scheduleId()));
    if (possibleSchedule.isEmpty()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, String.format("Schedule with id %s does not exist", command.scheduleId()));
    }

    String ehrObservationId = command.measurementTypeId();

    Optional<MeasurementType> systolicOpt = measurementTypeRepository.findByObservationIdAndMeasurementSubType(ehrObservationId, MeasurementSubTypeEnum.SYSTOLIC);
    Optional<MeasurementType> genericOpt = systolicOpt.isPresent() ? systolicOpt : measurementTypeRepository.findByObservationId(ehrObservationId);

    MeasurementType measurementType = genericOpt.orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_ERROR, "Measurement type not found for ehr observation id: " + ehrObservationId));

    var schedule = possibleSchedule.get();
    final var scheduleEntry = ScheduleEntry.createScheduleEntry(command.id(), measurementType, command.description(), MeasurementMode.valueOf(command.measurementMode()),
            command.intervalMinutes(), OffsetDateTime.parse(command.scheduledAt()), command.measurementsInBatch(), ScheduledMode.valueOf(command.scheduledMode()));

    if (StringUtil.isNullOrEmpty(command.id())) {
      schedule.addScheduleEntry(scheduleEntry);
    } else {
      final var currentScheduleEntry = schedule.getScheduleEntry(scheduleEntry.getId().toString());
      final var indexOf = schedule.getScheduleEntries().indexOf(currentScheduleEntry);
      schedule.getScheduleEntries().set(indexOf, scheduleEntry);
    }
    var persistedSchedule = backofficeScheduleRepository.save(schedule);
    return persistedSchedule.getScheduleEntries();
  }

  @Override
  public Schedule addUserToSchedule(String userId, String scheduleId) throws ValidationException {
    Optional<Schedule> schedule = backofficeScheduleRepository.findById(UUID.fromString(scheduleId));
    if (schedule.isPresent()) {
      scheduleUserRepository.save(new ScheduleUser(userId, scheduleId));
      return schedule.get();
    }
    throw new ValidationException(ErrorCode.VALIDATION_ERROR, String.format("Schedule with id %s does not exist", scheduleId));
  }

  @Override
  public void removeUserFromSchedule(String userId, String scheduleId) {
    scheduleUserRepository.deleteById(new ScheduleUserId(UUID.fromString(userId), UUID.fromString(scheduleId)));
  }

  @Override
  public Schedule deleteScheduleEntry(String scheduleId, String scheduleEntryId) throws ValidationException {
    Optional<Schedule> scheduleOption = backofficeScheduleRepository.findById(UUID.fromString(scheduleId));
    if (!scheduleOption.isPresent()) {
      throw new ValidationException(ErrorCode.VALIDATION_ERROR, String.format("Schedule with id %s does not exist", scheduleId));
    }
    final var schedule = scheduleOption.get();
    final var scheduleEntry = schedule.getScheduleEntry(scheduleEntryId);
    schedule.getScheduleEntries().remove(scheduleEntry);
    var persistedSchedule = backofficeScheduleRepository.save(schedule);
    scheduleEntryRepository.delete(scheduleEntry);
    return persistedSchedule;
  }
}
