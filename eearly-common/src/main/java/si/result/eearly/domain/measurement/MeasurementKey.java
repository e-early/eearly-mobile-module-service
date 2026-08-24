package si.result.eearly.domain.measurement;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import si.result.eearly.domain.enums.MeasurementMode;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class MeasurementKey {

  private final UUID measurementTypeId;
  private final UUID deviceId;
  private final UUID profileId;
  private final OffsetDateTime measuredAt;
  private final Double value;
  private final MeasurementMode measurementMode;

  public static MeasurementKey createFromMeasurement(Measurement measurement, UUID userId) {
    return new MeasurementKey(
        measurement.measurementTypeId(),
        null,
        userId,
        measurement.measurementTime().toOffsetDateTime(),
        measurement.magnitude(),
        MeasurementMode.SINGLE);
  }
}
