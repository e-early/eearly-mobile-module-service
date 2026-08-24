package si.result.eearly.ehr;

import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.MeasurementType;

import java.util.List;

public class EhrbaseMeasurementName {
  public static MeasurementType getMeasurementType(String archetypeId, String measurementName, List<MeasurementType> measurementTypes) {
    String archetypeIdWithoutVersion = archetypeId.substring(0, archetypeId.lastIndexOf('.'));
    return switch (archetypeIdWithoutVersion) {
      case "openEHR-EHR-OBSERVATION.pulse" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.HEART_RATE).findFirst().get();
      case "openEHR-EHR-OBSERVATION.pulse_oximetry" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.OXYGEN_SATURATION).findFirst().get();
      case "openEHR-EHR-OBSERVATION.lab_test-blood_glucose" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.BLOOD_GLUCOSE).findFirst().get();
      case "openEHR-EHR-OBSERVATION.blood_pressure" -> {
        var bloodPressureMeasurementType = measurementTypes.stream().filter(mt -> mt.getMeasurementSubType().toString().equalsIgnoreCase(measurementName)).findAny();
        if(bloodPressureMeasurementType.isEmpty()) {
          throw new IllegalArgumentException("Could not get blood pressure measurement type id for archetype " + archetypeId + " and measurement name " + measurementName);
        }
        yield bloodPressureMeasurementType.get();
      }
      case "openEHR-EHR-OBSERVATION.height" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.BODY_HEIGHT).findFirst().get();
      case "openEHR-EHR-OBSERVATION.body_weight" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.BODY_WEIGHT).findFirst().get();
      case "openEHR-EHR-OBSERVATION.body_temperature" -> measurementTypes.stream().filter(mt -> mt.getMeasurementType() == MeasurementTypeEnum.BODY_TEMPERATURE).findFirst().get();
      default -> throw new IllegalStateException("Could not get measurement type id for archetype: " + archetypeId);
    };
  }
}
