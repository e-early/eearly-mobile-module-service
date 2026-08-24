package si.result.eearly.repository;

import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;

import java.util.List;
import java.util.UUID;

public interface MeasurementRepository {

  List<Measurement> findAll(GetMeasurementsCommand command);

  MeasurementPage findPage(GetMeasurementsCommand command, int offset, int limit);

  void create(String composition, UUID ehrId);
}
