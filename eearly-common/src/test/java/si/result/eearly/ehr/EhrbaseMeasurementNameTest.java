package si.result.eearly.ehr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.MeasurementType;

class EhrbaseMeasurementNameTest {

  private static final List<MeasurementType> TYPES = Arrays.stream(
          new MeasurementTypeEnum[] {
              MeasurementTypeEnum.HEART_RATE_VARIABILITY,
              MeasurementTypeEnum.STRESS_LEVEL,
              MeasurementTypeEnum.GLUCOSE_VARIABILITY})
      .map(EhrbaseMeasurementNameTest::typeOf)
      .toList();

  private static MeasurementType typeOf(MeasurementTypeEnum type) {
    MeasurementType measurementType = new MeasurementType();
    measurementType.setMeasurementType(type);
    return measurementType;
  }

  @ParameterizedTest
  @CsvSource({
      "openEHR-EHR-OBSERVATION.eearly_heart_rate_variability.v0, HEART_RATE_VARIABILITY",
      "openEHR-EHR-OBSERVATION.eearly_stress_level.v0, STRESS_LEVEL",
      "openEHR-EHR-OBSERVATION.eearly_glucose_variability.v0, GLUCOSE_VARIABILITY"
  })
  void resolvesEearlySpecificArchetypes(String archetypeId, MeasurementTypeEnum expected) {
    MeasurementType resolved = EhrbaseMeasurementName.getMeasurementType(archetypeId, null, TYPES);

    assertThat(resolved.getMeasurementType()).isEqualTo(expected);
  }

  @Test
  void stillRejectsUnknownArchetypes() {
    assertThatThrownBy(() -> EhrbaseMeasurementName.getMeasurementType(
        "openEHR-EHR-OBSERVATION.stress_level.v1", null, TYPES))
        .isInstanceOf(IllegalStateException.class);
  }
}
