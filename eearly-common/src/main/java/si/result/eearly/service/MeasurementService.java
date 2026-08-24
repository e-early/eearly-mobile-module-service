package si.result.eearly.service;

import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;

import java.util.List;

public interface MeasurementService {

  void addBatchMeasurements(List<CreateMeasurementCommand> measurements);

//  Measurement updateMeasurement(UpdateMeasurementCommand command);

  List<Measurement> getMeasurementList(GetMeasurementsCommand command) throws ValidationException;

  MeasurementPage getMeasurementPage(GetMeasurementsCommand command, int offset, int limit)
      throws ValidationException;
}
