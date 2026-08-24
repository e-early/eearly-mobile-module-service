package si.result.eearly.mapper.ehr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.ehr.EhrbaseMeasurementName;
import si.result.eearly.genproto.Measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EhrGrpcMeasurementMapper {
  private final ObjectMapper objectMapper;

  public List<Measurement> toGrpcMeasurements(String data, List<MeasurementType> measurementTypeList) {
    List<Measurement> measurements = new ArrayList<>();
    try {
      JsonNode dataJson = objectMapper.readTree(data);
      JsonNode measurementsJson = dataJson.get("rows");
      if(measurementsJson.isArray()) {
        for(JsonNode measurementJson : measurementsJson) {
          String archetypeId = measurementJson.get(0).asText();
          String measurementName = measurementJson.get(1).asText();
          UUID measurementTypeId = EhrbaseMeasurementName.getMeasurementType(
                  archetypeId,
                  measurementName,
                  measurementTypeList).getId();
          double value = measurementJson.get(3).asDouble();
          String measurementBatchId = measurementJson.get(4).asText().split("::")[0];
          String measuredAt = measurementJson.get(5).asText();
          String deviceId = measurementJson.get(6).isNull() ? "" : measurementJson.get(6).asText();

          Measurement measurement = Measurement.newBuilder()
                  .setMeasurementTypeId(measurementTypeId.toString())
                  .setValue(value)
                  .setMeasurementBatchId(measurementBatchId)
                  .setMeasuredAt(measuredAt)
                  .setDeviceId(deviceId)
                  .build();

          measurements.add(measurement);
        }
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert ehrbase to grpc measurements", e);
    }
    return measurements;
  }
}
