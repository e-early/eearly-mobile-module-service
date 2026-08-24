package si.result.eearly.domain.measurement;

import java.util.UUID;

public record UpdateMeasurementCommand(
		UUID id,
		Double value,
		UUID userId) {
}
