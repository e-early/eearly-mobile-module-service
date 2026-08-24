package si.result.eearly.domain.enums;

import lombok.Getter;

@Getter
public enum MeasurementMode {
  SINGLE("SINGLE"),
  BATCH("BATCH"),
  CONTINUOUS("CONTINUOUS");

  private final String value;

  MeasurementMode(String value) {
    this.value = value;
  }
}
