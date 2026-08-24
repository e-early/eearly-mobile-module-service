package si.result.eearly.domain.measurement;

import java.util.List;
import java.util.UUID;

public record GetMeasurementsCommand(
    UUID ehrId,
    List<String> observationIds,
    String startDateTime,
    String endDateTime,
    List<MeasurementType> measurementTypes
) {

}