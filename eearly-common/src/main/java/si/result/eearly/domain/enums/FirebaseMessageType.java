package si.result.eearly.domain.enums;

import lombok.Getter;

@Getter
public enum FirebaseMessageType {
  NEW_SCHEDULE("NEW_SCHEDULE"),
  MEASUREMENT_REMINDER("MEASUREMENT_REMINDER");

  private final String value;

  FirebaseMessageType(String value) {
    this.value = value;
  }
}
