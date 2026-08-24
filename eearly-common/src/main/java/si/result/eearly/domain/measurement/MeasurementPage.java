package si.result.eearly.domain.measurement;

import java.util.List;

public record MeasurementPage(
    List<Measurement> measurements,
    long totalElements
) {
}
