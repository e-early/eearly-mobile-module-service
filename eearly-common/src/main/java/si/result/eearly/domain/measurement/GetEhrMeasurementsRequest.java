package si.result.eearly.domain.measurement;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetEhrMeasurementsRequest(
    @JsonProperty("q")
    String query,
    Integer fetch,
    Integer offset
) {

}
