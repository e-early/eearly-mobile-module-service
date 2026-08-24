package si.result.eearly.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Context;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.domain.schedule.CreateUpdateScheduleEntryCommand;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.domain.schedule.UpsertScheduleCommand;
import si.result.eearly.genproto.CreateUpdateScheduleEntryRequest;
import si.result.eearly.genproto.UpsertScheduleRequest;
import si.result.eearly.genproto.UpsertScheduleResponse;
import si.result.eearly.repository.MeasurementTypeRepository;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Mapper(uses = TimeMapper.class, collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface BackofficeScheduleMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "measurementType.id", target = "measurementTypeId")
  @Mapping(source = "scheduledMode", target = "scheduledMode")
  @Mapping(source = "measurementMode", target = "measurementMode")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "measurementsInBatch", target = "measurementsInBatch")
  @Mapping(source = "scheduledAt", target = "scheduledAt", qualifiedByName = "offsetDateTimeToString")
  @Mapping(source = "intervalMinutes", target = "intervalMinutes")
  si.result.eearly.genproto.ScheduleEntry toGrpcScheduleEntry(ScheduleEntry scheduleEntry);

  default si.result.eearly.genproto.GetScheduleResponse toGrpcScheduleResponse(Schedule schedule) {
    var responseBuilder = si.result.eearly.genproto.GetScheduleResponse.newBuilder();
    responseBuilder.setSchedule(toGrpcSchedule(schedule));
    return responseBuilder.build();
  }

  default si.result.eearly.genproto.Schedule toGrpcSchedule(Schedule schedule) {
    var scheduleBuilder = si.result.eearly.genproto.Schedule.newBuilder();
    scheduleBuilder.setId(schedule.getId().toString())
            .setName(schedule.getName())
            .setDescription(schedule.getDescription());
    if (schedule.getFacilityId() != null) {
      scheduleBuilder.setFacilityId(schedule.getFacilityId().toString());
    }
    if (schedule.getCaretakerId() != null) {
      scheduleBuilder.setCaretakerId(schedule.getCaretakerId().toString());
    }

    scheduleBuilder.setActive(schedule.isActive());
    return scheduleBuilder.build();
  }

  default UpsertScheduleCommand toUpsertCommand(UpsertScheduleRequest request, @Context MeasurementTypeRepository measurementTypeRepository) throws ValidationException {
    List<ScheduleEntry> entries = new ArrayList<>();
    for (CreateUpdateScheduleEntryRequest m: request.getMeasurementsList()) {
      entries.add(toScheduleEntryFromBackoffice(m, measurementTypeRepository));
    }
    return new UpsertScheduleCommand(
        request.getId(),
        request.getName(),
        request.getDescription(),
        request.getActive(),
        request.getUserId(),
        request.getCaretakerId(),
        entries
    );
  }

  default ScheduleEntry toScheduleEntryFromBackoffice(CreateUpdateScheduleEntryRequest request, @Context MeasurementTypeRepository measurementTypeRepository) throws ValidationException {
    String ehrObservationId = request.getMeasurementTypeId();

    Optional<MeasurementType> systolicOpt = measurementTypeRepository.findByObservationIdAndMeasurementSubType(ehrObservationId, MeasurementSubTypeEnum.SYSTOLIC);
    Optional<MeasurementType> genericOpt = systolicOpt.isPresent() ? systolicOpt : measurementTypeRepository.findByObservationId(ehrObservationId);

    MeasurementType measurementType = genericOpt.orElseThrow(() -> new ValidationException(ErrorCode.VALIDATION_ERROR, "Measurement type not found for ehr observation id: " + request.getMeasurementTypeId()));

      if (request.getScheduledAt().isEmpty()) {
        throw new ValidationException(ErrorCode.VALIDATION_ERROR, "scheduledAt is required for schedule entry");
      }
      OffsetDateTime scheduledAt = new TimeMapper() {}.stringToOffsetDateTime(request.getScheduledAt());

      return ScheduleEntry.createScheduleEntry(
        request.getId(),
        measurementType,
        request.getDescription(),
        si.result.eearly.domain.enums.MeasurementMode.valueOf(request.getMeasurementMode().name()),
        request.getIntervalMinutes(),
        scheduledAt,
        request.getMeasurementsInBatch(),
        si.result.eearly.domain.enums.ScheduledMode.valueOf(request.getScheduledMode())
    );
  }

  UpsertScheduleResponse toUpsertScheduleResponse(Schedule schedule);

  CreateUpdateScheduleEntryCommand toCreateUpdateScheduleEntryCommand(CreateUpdateScheduleEntryRequest request);

}
