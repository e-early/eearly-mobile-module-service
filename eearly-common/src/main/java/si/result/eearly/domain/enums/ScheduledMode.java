package si.result.eearly.domain.enums;

import lombok.Getter;

@Getter
public enum ScheduledMode {
  ONCE_A_DAY("ONCE_A_DAY"),
  AT_SPECIFIC_HOUR("AT_SPECIFIC_HOUR");

  private final String value;

  ScheduledMode(String value) {
    this.value = value;
  }
}
