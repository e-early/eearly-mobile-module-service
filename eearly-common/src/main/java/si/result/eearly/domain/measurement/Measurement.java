package si.result.eearly.domain.measurement;

import java.time.ZonedDateTime;
import java.util.UUID;
import si.result.eearly.domain.enums.MeasurementMode;

public record Measurement(
    UUID userId,
    String observationId,
    UUID measurementTypeId,
    String unit,
    Double magnitude,
    UUID batchId,
    ZonedDateTime measurementTime,
    UUID deviceId
) {

  public MeasurementKey toMeasurementKey() {
    return new MeasurementKey(
        measurementTypeId,
        deviceId,
        userId,
        measurementTime.toOffsetDateTime(),
        magnitude,
        MeasurementMode.SINGLE
    );
  }
}
