package si.result.eearly.mapper;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.MeasurementType;

import java.util.ArrayList;
import java.util.List;
import si.result.eearly.genproto.CreateMeasurement;

@Mapper(uses = TimeMapper.class)
public interface MeasurementMapper {

  default List<CreateMeasurementCommand> toCreateBatchCommand(List<CreateMeasurement> measurements, String userId) {

    List<CreateMeasurementCommand> commands = new ArrayList<>();
    for (CreateMeasurement measurement : measurements) {
      commands.add(toCreateCommand(measurement, userId));
    }
    return commands;
  }

  @Mapping(source = "measurement.measuredAt", target = "measuredAt", qualifiedByName = "stringToOffsetDateTime")
  CreateMeasurementCommand toCreateCommand(CreateMeasurement measurement, String userId);

  @Mapping(source = "measurementTypeId", target = "measurementTypeId")
  @Mapping(source = "magnitude", target = "value")
  @Mapping(source = "batchId", target = "measurementBatchId")
  @Mapping(source = "measurementTime", target = "measuredAt")
  @Mapping(source = "deviceId", target = "deviceId")
  si.result.eearly.genproto.Measurement toGrpcMeasurement(si.result.eearly.domain.measurement.Measurement measurement);

  @Mapping(source = "ehrId", target = "ehrId")
  @Mapping(source = "observationIds", target = "observationIds")
  @Mapping(source = "measurementTypes", target = "measurementTypes")
  @Mapping(source = "request.startDateTime", target = "startDateTime")
  @Mapping(source = "request.endDateTime", target = "endDateTime")
  GetMeasurementsCommand toGetMeasurementsCommand(si.result.eearly.genproto.GetMeasurementsRequest request, UUID ehrId, List<String> observationIds, List<MeasurementType> measurementTypes);
}
