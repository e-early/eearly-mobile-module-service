package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.mapper.MeasurementTypeMapper;
import si.result.eearly.service.MeasurementTypeService;
import si.result.eearly.genproto.GetMeasurementTypesResponse;

@Service
@RequiredArgsConstructor
public class MeasurementTypeServiceGrpcFacade {

  private final MeasurementTypeService measurementTypeService;
  private final MeasurementTypeMapper measurementTypeMapper;

  @Transactional(readOnly = true)
  public GetMeasurementTypesResponse getMeasurementTypes() {
    return measurementTypeMapper.toGetMeasurementTypesResponse(measurementTypeService.getMeasurementTypeList());
  }
}
