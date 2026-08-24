package si.result.eearly.domain.measurement;

import java.time.OffsetDateTime;

public record CreateMeasurementCommand(
        OffsetDateTime measuredAt,
        String measurementTypeId,
        Double value,
        String deviceId,
        String userId,
        int measurementGroupId,
        String measurementBatchId,
        String measurementMode
) {
}
