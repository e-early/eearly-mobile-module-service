package si.result.eearly.repository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.measurement.GetMeasurementsCommand;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementPage;
import si.result.eearly.ehr.EhrbaseClient;

@Repository
@RequiredArgsConstructor
public class EhrbaseMeasurementRepository implements MeasurementRepository {
  private final EhrbaseClient ehrbaseClient;

  @Override
  public List<Measurement> findAll(GetMeasurementsCommand command) {
    return ehrbaseClient.findAll(command);
  }

  @Override
  public MeasurementPage findPage(GetMeasurementsCommand command, int offset, int limit) {
    return ehrbaseClient.findPage(command, offset, limit);
  }

  @Override
  public void create(String contribution, UUID ehrId) {
    ehrbaseClient.createContribution(ehrId, contribution);
  }
}
