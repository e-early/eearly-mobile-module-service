package si.result.eearly.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.schedule.ScheduleEntry;
import si.result.eearly.genproto.GetScheduleResponse;

@Mapper(uses = TimeMapper.class, collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface ScheduleMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "measurementType.id", target = "measurementTypeId")
  @Mapping(source = "scheduledMode", target = "scheduledMode")
  @Mapping(source = "measurementMode", target = "measurementMode")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "measurementsInBatch", target = "measurementsInBatch")
  @Mapping(source = "scheduledAt", target = "scheduledAt", qualifiedByName = "offsetDateTimeToString")
  @Mapping(source = "intervalMinutes", target = "intervalMinutes")
  si.result.eearly.genproto.ScheduleEntry toGrpcScheduleEntry(ScheduleEntry scheduleEntry);

  default GetScheduleResponse toGrpcScheduleResponse(Schedule schedule) {
    var responseBuilder = GetScheduleResponse.newBuilder();
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
    return scheduleBuilder.build();
  }
}
