package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.mapper.MeasurementMapper;
import si.result.eearly.service.MeasurementTypeService;
import si.result.eearly.service.UserPrincipalService;
import si.result.eearly.service.MeasurementService;
import si.result.eearly.genproto.CreateMeasurementResponse;
import si.result.eearly.genproto.CreateMeasurementRequest;
import si.result.eearly.genproto.ErrorCode;
import si.result.eearly.genproto.GetMeasurementsForUserRequest;
import si.result.eearly.genproto.GetMeasurementsResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeasurementServiceGrpcFacade {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final MeasurementService measurementService;
  private final UserPrincipalService userPrincipalService;
  private final MeasurementMapper measurementMapper;
  private final MeasurementTypeService measurementTypeService;

  public CreateMeasurementResponse createBatchMeasurements(CreateMeasurementRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();

    measurementService.addBatchMeasurements(
        measurementMapper.toCreateBatchCommand(request.getMeasurementsList(), userId));
    CreateMeasurementResponse.Builder builder = CreateMeasurementResponse.newBuilder();
    return builder.build();
  }

  @Transactional(readOnly = true)
  public GetMeasurementsResponse getMeasurements(
      si.result.eearly.genproto.GetMeasurementsRequest request) {
    UUID ehrId = UUID.fromString(userPrincipalService.getUserPrincipal().getId());
    return getMeasurements(request.getMeasurementTypesList(), request.getStartDateTime(),
        request.getEndDateTime(), ehrId);
  }

  @Transactional(readOnly = true)
  public GetMeasurementsResponse getMeasurementsForUser(GetMeasurementsForUserRequest request) {
    UUID ehrId = UUID.fromString(request.getUserId());
    int page = Math.max(request.getPage(), DEFAULT_PAGE);
    int size = request.getSize() <= 0 ? DEFAULT_PAGE_SIZE : request.getSize();
    return getMeasurements(request.getMeasurementTypesList(), request.getStartDateTime(),
        request.getEndDateTime(), ehrId, page, size);
  }

  private GetMeasurementsResponse getMeasurements(List<String> requestedMeasurementTypes,
      String startDateTime, String endDateTime, UUID ehrId) {
    List<Measurement> measurements;
    var measurementTypes = measurementTypeService.getMeasurementTypeList();
    List<String> observationIds =
    requestedMeasurementTypes.stream().map(
        (measurementTypeString) ->
            measurementTypes.stream().filter(
                (measurementType) ->
                    measurementType.getMeasurementType() == MeasurementTypeEnum.valueOf(
                        measurementTypeString)).findFirst().get().getObservationId()
    ).toList();
    try {
      measurements = measurementService.getMeasurementList(
          new si.result.eearly.domain.measurement.GetMeasurementsCommand(
              ehrId, observationIds, startDateTime, endDateTime, measurementTypes));
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(),
          ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
    var measurementResponseBuilder = GetMeasurementsResponse.newBuilder();
    for (Measurement measurement : measurements) {
      measurementResponseBuilder.addMeasurements(measurementMapper.toGrpcMeasurement(measurement));
    }
    return measurementResponseBuilder.build();
  }

  private GetMeasurementsResponse getMeasurements(List<String> requestedMeasurementTypes,
      String startDateTime, String endDateTime, UUID ehrId, int page, int size) {
    var measurementTypes = measurementTypeService.getMeasurementTypeList();
    List<String> observationIds =
        requestedMeasurementTypes.stream().map(
            (measurementTypeString) ->
                measurementTypes.stream().filter(
                    (measurementType) ->
                        measurementType.getMeasurementType() == MeasurementTypeEnum.valueOf(
                            measurementTypeString)).findFirst().get().getObservationId()
        ).toList();

    MeasurementPage measurementPage;
    try {
      measurementPage = measurementService.getMeasurementPage(
          new si.result.eearly.domain.measurement.GetMeasurementsCommand(
              ehrId, observationIds, startDateTime, endDateTime, measurementTypes),
          (int) Math.min((long) page * size, Integer.MAX_VALUE),
          size);
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(),
          ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }

    var measurementResponseBuilder = GetMeasurementsResponse.newBuilder()
        .setPage(page)
        .setSize(size)
        .setTotalElements(measurementPage.totalElements())
        .setTotalPages((int) Math.ceil((double) measurementPage.totalElements() / size));
    for (Measurement measurement : measurementPage.measurements()) {
      measurementResponseBuilder.addMeasurements(measurementMapper.toGrpcMeasurement(measurement));
    }
    return measurementResponseBuilder.build();
  }
}
